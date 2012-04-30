package models

import java.util.Date

case class DataSourceEntry(
  sourceType: SourceType.Value,
  dataSourceName: String,
  label: String,
  url: String,
  modificationDate: Date
)