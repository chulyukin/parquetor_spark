package com.proto

import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.nio.charset.StandardCharsets
import java.util.Comparator
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.jline.terminal.Attributes.LocalFlag
import org.jline.terminal.{Attributes, Terminal, TerminalBuilder}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.types.{ArrayType,StructField}

/**
 *  Общие методы, переменные, и др. </br>
 *
 *  <h3>Константы: </h3>
 *  - цвета фона BG<цвет>;
 *  - цвета текста T<цвет>;
 *  - коды клавишей KEY_<код> ;
 *  - типы значений поля TYPE_<значение>.
 */
package object parquetor {
  // Цвета фона
  final val BGRAYL  = "\u001b[0m\u001b[48;5;8m"   // Background grey light
  final val BRED    = "\u001b[0m\u001b[48;5;1m"   // Background red
  final val BGRAY   = "\u001b[0m\u001b[48;5;59m"  // Background grey
  final val BORANGE = "\u001b[0m\u001b[48;5;3m"   // Background orange
  final val BGREEN  = "\u001b[0m\u001b[48;5;23m"  // Background green
  final val BGREEL  = "\u001b[0m\u001b[48;5;29m"  // Background light green
  final val BBLACKL = "\u001b[0m\u001b[48;5;119"  // Background light black
  final val BGRAYLL = "\u001b[0m\u001b[48;5;244m" // Background grey lighting light
  // Цвета текста
  final val TWHITEFADE   = "\u001b[38;5;66m"              // Text faded white
  final val TWHITE       = "\u001b[38;5;254m"             // Text simple white
  final val TWHITEBRIGHT = "\u001b[38;5;250m\u001b[01m"   // Text bright white
  final val TWHITEFLASH  = "\u001b[38;5;3m"               // Text flashing white
  final val TGREENLIGHT  = "\u001b[38;5;115m\u001b[10m"   // Text green light
  final val TWHITEBLINKB  = "\u001b[39;5;141m\u001b[01m"  // Text white blinked bold
  final val TWHITEBLINK   = "\u001b[39;5;118m"            // Text white blinked (no bold)
  // Клавиши
  final val KEY_BACKSPACE = 127
  final val KEY_TAB       = 9
  final val KEY_ENTER     = 10
  final val KEY_ESC       = 27
  final val KEY_UP        = 65
  final val KEY_DOWN      = 66
  final val KEY_LEFT      = 68
  final val KEY_RIGHT     = 67
  final val KEY_INSERT    = 50
  final val KEY_DELETE    = 51
  final val KEY_SPACE     = 32
  final val KEY_PGUP      = 53
  final val KEY_PGDOWN    = 54
  final val KEY_HOME      = 72
  final val KEY_END       = 70
  // Тип значения поля
  final val TYPE_S: String = "str"
  final val TYPE_I: String = "int"

//---------------------------------------------------- Терминал----------------------------------------------------
 /** Подготовка терминала. Метод проверяет текущие размеры терминала.
  *  В случае если, размеры менее требуемых - устанавливает требуемую для работы ширину и высоту.
  *  Создает объект - terminal и возвращает переменную типа Terminal
  *  Атрибуты терминала
  *   - ECHO - отображение символов при вводе (отключить)
  *   - ICANON - канонический (вводимые символы ожидают ENTER) режим работы (отключить)
  *
  *  @param width   ширина
  *  @param height  высота
  *  @return переменная типа Terminal
  */
  def prepareTerminal ( width: Int, height: Int): Terminal = {
    val tm = TerminalBuilder.builder().system(true).encoding(StandardCharsets.UTF_8).build()
    // Атрибуты терминала
    // ECHO - отображение символов при вводе (отключить)
    // ICANON - канонический (вводимые символы ожидают ENTER) режим работы (отключить)
    val newattrs = new Attributes(tm.getAttributes)
    newattrs.setLocalFlag(LocalFlag.ICANON,false)
    newattrs.setLocalFlag(LocalFlag.ECHO,false)
    tm.setAttributes(newattrs)
    //Проверка настройки терминала + установка размера
    if (tm.getWidth < width | tm.getHeight< height ) print(s"\u001b[8;${height+1};${width+1}t")
    // Возвращаемое значение - Terminal
    tm}
  /**
   * Строковая позиция курсора (для подстановки в print). Перемещает курсор на позицию координат.
   * Используется в строке print
   *
   * Принимает
   * x: Int - позиция в строке
   * y: Int - позиция в колонке
   */
  val pos: (Int, Int) => String = (x: Int, y: Int) => {s"\u001b[$y;${x}H"} // в строке

  /**
   * Устанавливает курсор в позицию координат. Делает курсор видимым при необходимости
   * @param x позиция в строке экрана
   * @param y позиция в колонке экрана
   * @param hide скрыть/показать курсор (true - скрыть)
   */
  def setPos(x: Int, y: Int, hide: Boolean = true ): Unit = { // установить позицию курсора
    print(pos(x,y) + (if (hide) "\u001b[?25l" else "\u001b[?25h"))
    print(s"\u001b[3 q") // мигающий курсор "_"
  }

  /** Очистка экрана */
  def clearScrean(): Unit = print("\u001b[2J\u001b[H")
  // Закрыть приложение - восстановить терминал

  /**
   * Закрывает приложение. Восстанавливает консольный терминал. Освобождает ресурсы (terminal, spark)
   * @param terminal объект Terminal
   * @param spark spark-сессия
   */
  def closeApplication (terminal: Terminal, spark: SparkSession): Unit = {
    if (spark == null) spark.close()          // закрыть сессию spark
    if (terminal == null) terminal.close()    // закрыть терминал
    print("\u001b[0m")   // вернуть цвет
    print("\u001b[?25h") // Сделать курсор видимым
    clearScrean()
    System.out.flush()
    sys.exit(1)
  }

  /**
   * Разбор аргументов командной строки. Метод анализирует параметр вызова --parquet-dir (путь к файлам .parquet)
   * Usage params: [--parquet-dir <str value>]
   * @param args директория с файлами parquet для перекодировки.
   * @return путь к source файлам
   */
  def parseArgs(args: Array[String]): String = {
    val usage = "ERROR: Usage params: [--parquet-dir <str value>]"
    if (args.length < 3) println(usage)
    var sourceDir = ""
    try {
      // Перекладывание пар аргументов в Map (key: имя параметра, value - значение параметра)
      val argsMap = args.map(x => x).grouped(2).map(x => (x(0), x(1))).toMap
      sourceDir = argsMap("--parquet-dir")
      } catch {case _: Throwable => println("ERROR: Application was closed."); sys.exit(1)}
    println(s"Source parquet path: $sourceDir")
    truncPathToReal(sourceDir)
  }
//------------------------------------------------- Обработка пути -------------------------------------------------
  /**
   * Преобразуют путь вида ~/<путь> в /user/home/<путь>;
   */
  val truncPathToReal: String => String = (path: String)  =>  path.replaceFirst("^~", System.getProperty("user.home"))
  /**
   * Преобразуют путь вида /user/home/<путь> в ~/<путь>;
   */
  val realPathToTrunc: String => String = (path: String)  =>  path.replaceFirst("^"+System.getProperty("user.home"),"~")

  /**
   * Проверка пути на существование
   *
   * Принимает - путь
   * Возвращает true, если путь существует
   */
  val pathExist: String => Boolean = (path: String) => Files.exists(Paths.get(path))

  /**
   * Чтение файлов .parquet
   * @param inputPath строка пути к папке с файлами
   * @return Array[<имя файла>, <длина в бйтах>]
   */
  def parquetFiles(inputPath: String): Array[(String,Long)] = {
    val parquetFiles = new File(inputPath)
      .listFiles()
      .filter(_.getName.endsWith(".parquet"))     // Фильтр по расширению
      .map(file => (file.getName, file.length())) // Имя файла и размер в килоБайтах
    parquetFiles
  }
 //------------------------------------------------ Работа с файлами ------------------------------------------------
  /**
   * Перевод размера файла в human-readable
   * @param bytes размер файла в байтах
   * @return строка в формате <число><ед. изм.>
   */
 def bytesHR (bytes: Long): String = {
   val us = Array("bytes","Kb","Mb","Gb") // units (ед. изм)
   if (bytes < 1024) s"${bytes}b"
   else {
     val exp = (Math.log(bytes)/Math.log(1024)).toInt
     val value = bytes/Math.pow(1024,exp)
     if (value% 1 == 0) f"${value.toLong}${us(exp)}" else f"$value%.1f${us(exp)}"
   }
 }

  /* Функция переименования единичного файла
      parPath: String - основной путь, в котором сохраняются файлы csv
      filename: String - имя файла csv, созданного saprk dataframe - write - csv
    При вызове spark dataframe.coalesce(1)...write... csv, Spark создает для каждого сохраняемого объекта отдельную
    директорию, с именем сохраняемого csv, в которой будет сохранен файл part-<нечитаемая строка>.csv
    Функция производит поиск сохраненных файлов part-<нечитаемая строка>.csv, переименовывает их в целевое название и
    копирует из директории в основную папку
  */
 private def copyRenameCsvFiles(parPath: String, filename: String): Unit = {
    // Чтение всех файлов .csv в директории по пути из parPath/filename
    val dir = new File(s"$parPath/$filename")
    // Поиск файлов .csv, которые имеют префикс part-
    val partFile = dir.listFiles().find(f => f.isFile && f.getName.startsWith("part-") && f.getName.endsWith(".csv") )
    // Копирование и переименование
    partFile match {
      case Some(pFile) => 
        val targetPath = Paths.get(parPath, s"${filename.replaceAll("\\.[^.]*$", "")}.csv")
        Files.copy(pFile.toPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
      case None => println("Файлы *.csv не найдены")
                  }
   }

 /* Функция удаления директорий с файлами по переданному в параметре pathToDelete пути
    pathToDelete: String - путь для удаления всех зависимых папок и файлов
   По пути, переданному в параметре pathToDelete, удаляются все файлы и директории, при этом сам путь pathToDelete
   не удаляется
 */
 private def removeFolders(pathToDelete: String ): Unit =
  Files.list(Paths.get(pathToDelete)).forEach{ path => 
              if (Files.isDirectory(path)) Files.walk(path).sorted(Comparator.reverseOrder()).forEach(Files.delete) }
  
 /* Функция сохранения текстового файла
    path: String - путь для сохранения файла
    file: String - имя сохраняемого файла
    text: String - текст, который требуется сохранить в файле
  Файл с именем из <file> и расширением .txt (если передано другое расширение в имени, оно будет заменено на .txt),
  с текстом из <text> сохраняется по пути из <path> с перезаписью файла, если он существовал ранее.
 */
 private def saveTextFile (path: String, file: String, text: String): Unit = {
    // Внутрення функция печати текста в файл
    def printToFile(f: java.io.File) (op: java.io.PrintWriter => Unit): Unit = {
        val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
        }
    // Создание файла и вызов печати в файл
    printToFile(new File(s"$path/${file.replaceAll("\\.[^.]*$", "")}.txt")) { p => p.println(text) }
   }

  //------------------------------------------------- Spark -------------------------------------------------
  // Инициализация spark

  /**
   * Создание spark-сессии
   * @param appName имя сессии spark
   * @return переменная SparkSession
   */
  def createSparkSession(appName: String): SparkSession = {
    Logger.getLogger("org").setLevel(Level.OFF)
    SparkSession
      .builder()
      .config("spark.sql.caseSensitive", value = true)
      .config("spark.sql.session.timeZone", value = "UTC")
      .appName(appName)
      .getOrCreate()
   }

  /**
   * Конвертация файлов parquet в csv
   * @param path целевой путь (output)
   * @param parquets Array[(String,Long,Long,String, DataFrame) - массив - структура файлов:
   *                 - filename имя файла parquet
   *                 - size - размер файла parquet
   *                 - count - количество записей в файле
   *                 - data.schema.treeString - схема файла parquet
   *                 - data - Spark DataFrame с прочитанным файлом
   * @param delimeter разделитель (1 символ)
   * @param limitSize количество записей в выходном файле или строка "All" - все записи
   * @param y_pos номер строки экрана для выводя сообщения
   */
  def convertToCsv(path: String, parquets: Array[(String,Long,Long,String, DataFrame)]
                 ,delimeter: String, limitSize: String, y_pos: Int ): Unit = {
    // В DataFrame могут быть колонки типа Array - преобразование в String
    def arrColsCastString(df: DataFrame): DataFrame = {
      val arrCols = df.schema.fields.collect {case StructField(name, t: ArrayType, _ ,_) => name }
      arrCols.foldLeft(df) {(acc, colName) => acc.withColumn(colName, acc(colName).cast("string"))}
    }
    val limitLn: Int = if (limitSize == "All") -1 else limitSize.toInt
    val len = parquets.length
    parquets.zipWithIndex.foreach{
        case ((filename: String, size: Long, count: Long, schema: String, df: DataFrame),i: Int) =>
          print(pos(2,y_pos) +BRED+TWHITEFLASH+TWHITEBLINKB +s"Converting " +BRED+TWHITEBRIGHT+
                        s"($i/$len): " + BRED + filename )
          arrColsCastString(df = if (limitLn > 0) df.limit(limitLn) else df)
            .coalesce(1).write
            .mode("overwrite")
            .option("header", "true")
            .option("delimiter", delimeter)
            .csv(s"$path/$filename")

          val statString = s"file: $filename\n"+
                           s"size (kB): ${size/1024}\n"+
                           s"count rows: $count\n"+
                           s"$schema"
          // Копирование файла в основную директорию и переименование
          copyRenameCsvFiles(path, filename)
          // Сохранение статистики
          saveTextFile(path = path, file = filename, text = statString)
          // Удаление исходных папок
          removeFolders(pathToDelete = path)
    }
  }
}