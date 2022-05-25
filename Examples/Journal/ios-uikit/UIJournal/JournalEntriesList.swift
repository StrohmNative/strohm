import UIKit
import RxSwift
import RxCocoa
import RxDataSources
import Differentiator

class JournalEntriesList: UIViewController {
    @IBOutlet private var tableView: UITableView!

    let disposeBag = DisposeBag()

    var dataSource: RxTableViewSectionedAnimatedDataSource<MySection>?

    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "Cell")

        let dataSource = RxTableViewSectionedAnimatedDataSource<MySection>(
            configureCell: { ds, tv, _, item in
                let cell = tv.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell(style: .default, reuseIdentifier: "Cell")
                cell.textLabel?.text = item.title
                return cell
            }
        )

        self.dataSource = dataSource

        let sections = [
            MySection(header: "First section", items: [
                JournalEntry(id: "1", title: "one", text: "one", created: Date()),
                JournalEntry(id: "2", title: "two", text: "two", created: Date())
            ])
        ]

        Observable.just(sections)
            .bind(to: tableView.rx.items(dataSource: dataSource))
            .disposed(by: disposeBag)
    }
}

struct MySection {
    var header: String
    var items: [JournalEntry]
}

extension MySection: AnimatableSectionModelType {
    typealias Item = JournalEntry

    var identity: String {
        return header
    }

    init(original: MySection, items: [JournalEntry]) {
        self = original
        self.items = items
    }
}
