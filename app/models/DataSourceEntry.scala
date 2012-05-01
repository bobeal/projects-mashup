package models

import java.util.Date

case class DataSourceEntry(
  sourceType: SourceType.Value,
  dataSourceName: String,
  label: String,
  url: String,
  modificationDate: Date
)  extends Ordered[DataSourceEntry] {
  def compare(that: DataSourceEntry) = that.modificationDate.compareTo(this.modificationDate)
}
