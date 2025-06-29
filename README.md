# parquetor
 Репозиторий демо-проекта Scala Spark для сборщика SBT Aassembly. Приложение **"Parquetor"** - конвертер 
 файлов формата parquet в формат csv с консольным оконным интерфейсом.
 
## Overview
Это демонстрационный проект **Scala** **Spark** для сборщика **SBT assembly** (**«fat» jar**), 
с примером кода малой сложности. Приложение запускается в среде фреймворка **Spark** (spark-submit). 
Пример предназначен для использования в образовательных целях 
или как основа для других проектов **Scala Spark**. Содержит необходимую структуру для сборки исполняемого приложения **java --jar**. 
В описании к проекту приведены основные пояснения настроек и кода. Для проекта используется локально 
установленный **Spark ver.3.5.5**. 

## Essence
Приложение — конвертер файлов формата **parquet** в формат **csv**, с *console-оконным* интерфейсом. Производится чтение **.parquet** файлов по пути, 
из параметра запуска, конвертация исходных файлов формат **csv** (разделитель задается пользователем) и сохранение результата по пути, 
указанному в поле-параметре. В отдельном поле указывается размер сэмпла (количество строк), с возможностью значения «all», при котором результирующий csv 
будет содержать все строки исходного файла parquet. Для каждого конвертируемого файла формируется простая статистика по исходному 
файлу .parquet (количество записей, структура и размер исходного файла). Приложение имеет console-оконный интерактивный интерфейс (80x25 колонок/строк), 
с двумя основными окнами: "Info" и "Convert", и модальным окном с информацией о единичном файле .parquet. Вывод окон управляется средствами 
головного "MENU:". При успешном запуске приложения, выводится окно "Info" в котором можно просмотреть список файлов (.parquet) и параметры отдельного файла. 
В окне "Convert" задаются параметры конвертации и производится сама конвертация файлов в .csv, согласно заданным параметрам. 
Выход из приложения осуществляется средствами пункта меню "Exit"

## Оконный интерфейс  
Навигация по головному меню осуществляется клавишей **[TAB]**.  

**[Меню "Info"](README/info.md)**  
**[Меню "Convert"](README/convert.md)**  
**[Меню "Exit"](README/exit.md)**  
  
## Requirements
| Инструмент    |Версия|Комментарий|Ресурс|
|:--------------|:-|:-|:-|
| Java          |openjdk-17|Openjdk-17 - это (Open Java Development Kit) - это бесплатная реализация платформы Java Standard Edition (Java SE) с открытым исходным кодом |https://openjdk.org/|
| Scala         |2.12.19|Проверенная стабильная версия, хорошо совместима с OoenJDK-17 (в качестве альтернативы можно использовать 2.12.18 )|https://scala-lang.org/|
| SBT           |1.11.2| Scala build tool. В качестве альтернативы можно использовать не ниже 1.6.1|https://docs.scala-lang.org/overviews/scala-book/scala-build-tool-sbt.html|  
| Apache Spark  |3.5.6| Фреймворк с открытым исходным кодом для реализации распределённой обработки данных, входящий в экосистему проектов Hadoop.|https://spark.apache.org/|

Про организацию рабочей среды, в которой собирается данный проект, можно прочитать здесь: https://github.com/chulyukin/howto-base-notes)

## Assembly and Launch
1) Загрузить проект в директорию проектов IntelliJ IDEA 2025.1.2 (Community Edition) (_например в ~/IdeaProjects):
```console
cd ~/IdeaProjects
git clone https://github.com/chulyukin/parquetor_spark.git
```
2) Открыть проект в среде IntelliJ IDEA 2025.1.2  
3) Далее выполнить команду сборки
```console
assembly
```
** для получения документации можно выполнить команду doc
В директории target/scala-2.12, будет собран файл **parquetor-assembly-0.1.0.jar**
```console
ls ~/IdeaProjects/spark-simple-parquet/target/scala-2.12
# classes parquetor-assembly-0.1.0.jar  sync  update  zinc classes
```
4) Запуск **parquetor-assembly-0.1.0.jar** на исполнение

|Параметр|Описание|Пример|
|:-|:-|:-|
|--source-dir| Строка. Путь к директории файлов .parquet для конвертирования|--source-dir example_parquet|

```console
spark-submit \
--master local[2] \
--class com.proto.parquetor.Main \
--driver-java-options "-Dlog4j.configuration=file:IdeaProjects/Parquetor_spark/src/main/resources/log4j2.properties" \
--conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j2.properties" ~/IdeaProjects/Parquetor_spark/target/scala-2.12/parquetor-assembly-0.1.0.jar \
--parquet-dir ~/IdeaProjects/Parquetor_spark/example_parquet

```
Во время выполнения, задача отображается в пользовательском интерфейсе мастера Spark. Пользовательский интерфейс можно просматривать по адресу localhost:4040. http://localhost:4040/jobs/ 

5) Результат (в случае запуска на тестовых источниках _example_parquet_)
```console
ls ~/IdeaProjects/Parquetor_spark/example_parquet/
# example_150plus_columns.parquet  example_second.parquet  userdata3.parquet example_first.parquet  example_third.parquet   yellow_tripdata_2023-08.parquet

```
Будет создана директория output с файлами .csv
```console
ls output/
# example_150plus_columns.csv  example_second.csv  sample3.csv    users.csv
# example_150plus_columns.txt  example_second.txt  sample3.txt    users.txt
# example_first.csv            example_third.csv   userdata3.csv  yellow_tripdata_2023-08.csv
# example_first.txt            example_third.txt   userdata3.txt  yellow_tripdata_2023-08.txt

```
Пример отчета статистики файла .parquet
```console
cat output/yellow_tripdata_2023-08.txt
```
```text
file: yellow_tripdata_2023-08.parquet
size (kB): 47023
count rows: 2824209
root
 |-- VendorID: integer (nullable = true)
 |-- tpep_pickup_datetime: timestamp_ntz (nullable = true)
 |-- tpep_dropoff_datetime: timestamp_ntz (nullable = true)
 |-- passenger_count: long (nullable = true)
 |-- trip_distance: double (nullable = true)
 |-- RatecodeID: long (nullable = true)
 |-- store_and_fwd_flag: string (nullable = true)
 |-- PULocationID: integer (nullable = true)
 |-- DOLocationID: integer (nullable = true)
 |-- payment_type: long (nullable = true)
 |-- fare_amount: double (nullable = true)
 |-- extra: double (nullable = true)
 |-- mta_tax: double (nullable = true)
 |-- tip_amount: double (nullable = true)
 |-- tolls_amount: double (nullable = true)
 |-- improvement_surcharge: double (nullable = true)
 |-- total_amount: double (nullable = true)
 |-- congestion_surcharge: double (nullable = true)
 |-- Airport_fee: double (nullable = true)
```
## Project architecture
Структура кода
```tree
.
├── build.sbt
├── example_parquet
│   ├── example_150plus_columns.parquet
│   ├── example_first.parquet
│   ├── ....
│   └── yellow_tripdata_2023-08.parquet
├── LICENSE
├── project
│   ├── build.properties
│   └── plugins.sbt
├── README.md
└── src
    └── main
        ├── resources
        │   └── log4j2.properties
        └── scala
            └── com
                └── proto
                    └── parquetor
                        ├── package.scala
                        ├── ClassItems.scala
                        ├── Main.scala
                        ├── Menu.scala
                        ├── MessageBox.scala
                        ├── Templates.scala
                        ├── WindowConvert.scala
                        └── WindowInfo.scala                        
```
**Файловая структура в src/main/scala/com/simple/**

|Файл| Описание                                                                                                                                                                                                                |
|:-|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|package.scala| Файл определения пакетного объекта package object parquetor. Контейнер для функций, переменных и псевдонимов типов, которые доступны в элементах дочернего пакета com.proto.parquetor по прямому доступу (без импорта). |
|ClassItems.scala| Класы компонетов окон. Реализуется функционал поля, checkbox кнопки|
|Main.scala| Точка входа в программу - object Main extends App                                                                                                                                                           |  

Более подробное описание программных элементов можно получить с помощью команды **sbt doc** (командв doc в sbt-shell IntelliJ IDEA)

## Example test parquet
Данные для тестового примера находятся в директории  example_parquet
Для примера выложены два файла .parquet: 
 - example_parquet/example_150plus_columns.parquet
 - example_parquet/example_first.parquet
 - example_parquet/example_second.parquet
 - example_parquet/example_third.parquet
 - example_parquet/userdata3.parquet
 - example_parquet/yellow_tripdata_2023-08.parquet 

Источник данных — TLC Trip Record Data.  
TLC Trip Record Data — это набор данных, содержащий подробную информацию о поездках на такси в Нью-Йорке. Данные были загрузжены с [сайта https://www.nyc.gov](https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page?source=post_page-----80e2680d3528--------------------------------). Это записи о поездках на желтом такси (Yellow Taxi Trip).

```markdown
Источник данных

Данный репозиторий содержит файлы формата Parquet, созданные на основе открытого набора данных о поездках такси и автомобилей с водителем (TLC Trip Record Data) Нью-Йорка.
Данный репозиторий содержит файлы формата Parquet, сгенерированные вручную для тестирования данного приложения.  

Источник: 
NYC TLC Trip Record Data: https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
```
