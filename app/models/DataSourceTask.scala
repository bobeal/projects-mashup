package models

import java.util.Date

case class DataSourceTask(
  sourceType: SourceType.Value,
  dataSourceName: String,
  label: String,
  url: String,
  dueDate: Date
) extends Ordered[DataSourceTask] {
  def compare(that: DataSourceTask) = that.dueDate.compareTo(this.dueDate)
}
