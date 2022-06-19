import Foundation
import UIKit
import StrohmNative
import RxSwift

class JournalEntryDetail: UIViewController {
    var viewModel: SimpleSubject<JournalEntry>!
    let disposeBag = DisposeBag()

    @IBOutlet weak var textView: UITextView!
    var dirty: BehaviorSubject<Bool> = .init(value: false)
    @IBOutlet var saveButton: UIBarButtonItem!
    @IBOutlet var renameButton: UIBarButtonItem!
    @IBOutlet weak var dateLabel: UILabel!

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.textView.text = self.viewModel.data.text
        textView.rx.text.subscribe(onNext: { [unowned self] text in
            let isDirty = (text != self.viewModel.data.text)
            print("isDirty? \(isDirty)")
            self.dirty.onNext(isDirty)
        }).disposed(by: disposeBag)

        viewModel.publisher.asObservable().subscribe(onNext: { entry in
            self.navigationItem.title = entry.title
            self.dateLabel.text = entry.created.formatted(date: .abbreviated, time: .shortened)
        }).disposed(by: disposeBag)

        self.dirty.distinctUntilChanged().subscribe(onNext: { dirty in
            print("dirty: \(dirty)")
            self.navigationItem.rightBarButtonItem = dirty
                ? self.saveButton
                : self.renameButton
        }).disposed(by: self.disposeBag)

        view.keyboardLayoutGuide
            .topAnchor
            .constraint(equalToSystemSpacingBelow: textView.bottomAnchor, multiplier: 1)
            .isActive = true
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        viewModel = nil
    }

    func set(entry: JournalEntry) {
        viewModel = SimpleSubject(initialData: entry,
                                  propName: "entry",
                                  propPath: ["entries", entry.id])

        self.navigationItem.title = entry.title
    }

    @IBAction func rename() {
        let alert = UIAlertController(title: "Change title", message: nil, preferredStyle: .alert)
        alert.addTextField { textField in
            textField.placeholder = "Enter some text"
            textField.text = try? self.viewModel.publisher.value().title
        }
        alert.addAction(UIAlertAction(title: "Cancel", style: .destructive) { _ in
            alert.dismiss(animated: true)
        })
        alert.addAction(UIAlertAction(title: "Rename", style: .default) { _ in
            if let textField = alert.textFields?.first, let text = textField.text {
                self.onRename(newTitle: text)
            }

            alert.dismiss(animated: true)
        })
        DispatchQueue.main.async {
            self.present(alert, animated: true)

        }
    }

    @IBAction func save() {
//        self.editMode = false
        var updatedEntry = viewModel.data
        updatedEntry.text = self.textView.text
        try! StrohmNative.default.dispatch(type: "update-entry", payload: updatedEntry)
        self.navigationController?.popViewController(animated: true)

    }

    func onRename(newTitle: String) {
        if let currentEntry = try? self.viewModel.publisher.value() {
            var updatedEntry = currentEntry
            updatedEntry.title = newTitle

            DispatchQueue.main.async { [weak self] in
                self?.viewModel.publisher.onNext(updatedEntry)
                try! StrohmNative.default.dispatch(type: "update-entry", payload: updatedEntry)
            }
        }
    }
}
