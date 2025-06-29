package com.proto.parquetor

import org.jline.terminal.Terminal
import org.apache.spark.sql.DataFrame
import com.proto.parquetor.Menu._
import com.proto.parquetor.WindowInfo._
import com.proto.parquetor.WindowConvert._
import com.proto.parquetor.Templates._
import com.proto.parquetor.MessageBox._

/**
 * Main объект - Основной метод. (точка входа)
 *  - управление главным меню
 *  - загрузка окон
 */
object Main extends App {
  // Незапланированный выброс из приложения
  sys.addShutdownHook {
    print("\u001b[?25h") // Сделать курсор видимым
    System.out.flush() }

  // Создать окно по шаблону
  private val createTemplateWindow: () => Unit = () => {
    // Верхняя рамка
    print(HEADER_STATIC) // высота 2
    // Меню (выделить первый пункт)
    MenuBar(0, 0)
    //Заголовок "Путь"
    print(PATH_HEADER)
    // Само окно высота W_HIGHT-3
    print(WINDOW_TEMPLATE)
  }

  // Чтение параметров
  /** Чтение параметров */
  private val inputPath: String = parseArgs(args = args)
  setPathString(inputPath) // Установить экранную переменную
  // Инициализация Spark session
  private val spark = createSparkSession("Spark parquetor")
  spark.sparkContext.setLogLevel("ERROR")
  // Подготовка экрана
  private val terminal: Terminal = prepareTerminal(width = W_WID, height = W_HIG)
  clearScrean()
  if (!pathExist(inputPath)) {
    messageBox(terminal, s"Path: $pathStr", header=" ERROR: Path not found ", buttons = MB_OK)
    closeApplication(terminal,spark)}
  // Создать окно по шаблону
  createTemplateWindow()
  // Статус загрузки
  drawLoadingStatus()
  // Прочитать parquet - файлы
  private val fileAtr: Array[(String,Long,Long,String, DataFrame)] = parquetFiles(inputPath).map { case (filename, size) =>
    val data = spark.read.parquet(path=s"$inputPath/$filename")
    (filename, size, data.count, data.schema.treeString, data) }
  if (fileAtr.length==0) {
    messageBox(terminal,"ERROR: Parquet files not found", header= s"Path: $pathStr", buttons = MB_OK)
    closeApplication(terminal,spark)}
  // Меню
  private var menuItemIdx: Int = 0 // Текущий элемент меню
  private var selectedIdx: Int = 0 // Выбранный элемент меню
  private var pressed_enter = true         // Нажата клавиша ENTER
  // Обработчик Меню
  while (true) {
    MenuBar(menuItemIdx, selectedIdx) // отобразить меню
    if (!pressed_enter){ // обработка клавиш меню
      val key= terminal.input().read()
      key match {
        case  KEY_TAB =>  selectedIdx = (selectedIdx + 1) % menuItems.length
        case  KEY_ENTER => menuItemIdx = selectedIdx; pressed_enter = true
        case _ =>  }
    }
    else {  // обработка возврата из окон
      pressed_enter = false
      if (selectedIdx==0) windowInfo (terminal,fileAtr)
      else if (selectedIdx==1) windowConvert(terminal, fileAtr)
      else if (selectedIdx==2)  {
        if (messageBox(terminal,"Are you exit?", backgound= BGRAYL)) closeApplication (terminal, spark)
        restoreDrawnWindow(4)
      }
      // Переход на следующий пункт меню
      selectedIdx = (selectedIdx + 1) % menuItems.length
    }
  }
}