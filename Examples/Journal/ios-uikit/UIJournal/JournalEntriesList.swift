import UIKit
import RxSwift
import RxCocoa
import RxDataSources
import Differentiator
import StrohmNative

class JournalEntriesList: UITableViewController {
//    @IBOutlet private var tableView: UITableView!

    let disposeBag = DisposeBag()
    var viewModel: KeyedArraySubject<JournalEntry>!

    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.delegate = nil
        self.tableView.dataSource = nil

        viewModel = KeyedArraySubject(initialEntries: [],
                                      propName: "entries",
                                      propPath: ["entries"])
        viewModel.sorter = { (e1, e2) -> Bool in
            e1.created > e2.created
        }

//        let data = Observable<[JournalEntry]>.just([
//            JournalEntry(id: "1", title: "One", text: "one", created: Date()),
//            JournalEntry(id: "2", title: "Two", text: "two", created: Date())
//        ])

        viewModel.publisher.bind(to: tableView.rx.items(cellIdentifier: "Cell")) { index, entry, cell in
            cell.accessoryType = .disclosureIndicator
            cell.textLabel?.text = entry.title
            cell.detailTextLabel?.text = entry.created.formatted(date: .abbreviated, time: .shortened)
        }.disposed(by: disposeBag)

        tableView.rx.itemDeleted.subscribe { [unowned self] indexPath in
            self.deleteEntry(at: indexPath)
        }.disposed(by: disposeBag)
    }

    @IBAction func newEntry(_ sender: UIBarButtonItem) {
        StrohmNative.default.dispatch(type: "new-entry")
    }

    func deleteEntry(at indexPath: IndexPath) {
        if let entries = try? viewModel.publisher.value() {
            let id = entries[indexPath.row].id
            try! StrohmNative.default.dispatch(type: "remove-entry", payload: ["entry/id": id])
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let targetVc = segue.destination as? JournalEntryDetail,
            let index = self.tableView.indexPathForSelectedRow?.row,
            let entry = try? viewModel.publisher.value()[index] {
            targetVc.set(entry: entry)
        }
    }
}
