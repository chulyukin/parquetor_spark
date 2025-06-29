package com.proto.parquetor

/* Подсказки: "\u2550": ═; "\u2563": ═╣; "\u255A": ╚; "\u2554": ╔; "\u2560": ╠═; "\u2551": ║;
              "\u2557": ╗; "\u255D": ╝; :├  ⌛ ⏬ */
/** Псевдографические шаблоны и атрибуты окон */
object Templates {
  /** Ширина окна */
  final val W_WID: Int = 80 // Ширина окна (min 70)
  /** Высота окна */
  final val W_HIG: Int = 25 // Высота  окна (min 24)

  // ANSI-escape-коды для фона и текста
  val C_GRD: String = BGREEN         // Основной цвет фона окна
  val C_MNU: String = BGREEL         // Цвет меню
  val T_GRD: String = TWHITEFADE     // Основной цвет текста окна (рамки, текст рамки)
  val T_STT: String = TWHITEBRIGHT   // Статический текст
  val C_BTN: String = BORANGE        // Кнопки на всплывающих окнах

  /** Вывод мигающего Loading в позиции 2 (исчезнет после перерисовки) */
  val drawLoadingStatus: () => Unit = ()=> {setPos(x=2,y=5); print(TWHITEBLINK +"Loading...")}
  /** Строка пути для отображения */
  var pathStr = ""
  // Инициализация переменной экранной строки пути
  /**
   * Инициализация переменной экранной строки пути (pathStr)
   * Преобразует путь для отображения на экране и присваивает его переменной pathStr
   *
   * Параметры:
   *  - path: String - реальный путь к файлам
   *
   */
  val setPathString: String => Unit = (path: String) => {
    // Длина пути для отрисовки (9/11 ширины)
    val path_ = realPathToTrunc(path)
    pathStr = if (path_.length > W_WID*9/10) "..." + path_.substring(path_.length -W_WID*9/11-3) else path_
  }
  // Цвет основного окна и текста
  val BTGRD: String = C_GRD+T_GRD

  /*--------------- Шаблон окна -----------------------------*/
  /**
   * Статическая верхняя часть окна (заголовок, меню, путь) (y=0)
   */
  val HEADER_STATIC: String = pos(1,1) +
              BTGRD +s"╔═════════════════════"  +"═"*(W_WID-35)+ "(Parquetor)═╗\n"+
              BTGRD +s"║ ${T_STT}MENU:$T_GRD  " +" "*(W_WID-10)            + "║\n"+
              BTGRD +s"╠═${T_STT}Path:$T_GRD═"  +"═"*(W_WID-10)            +"═╣\n"

  /**
   * Статический заголовок "Путь" (y=3)
   */
  lazy val PATH_HEADER: String = pos(1,4) +
              BTGRD +s"║ $C_GRD$pathStr$BTGRD"+" "*(W_WID-pathStr.length-3)+"║\n"
  // Отдельная строка
  /**
   * Шаблон одной строки окна
   */
  val WINDOW_ROW: String =
              BTGRD +s"║"                                    +" "*(W_WID-2)             +"║\n"
  /**
   * Основной шаблон окна (y=6)
  */
  val WINDOW_TEMPLATE: String = Range.inclusive(6, W_HIG).map(_=>
                 WINDOW_ROW).mkString +
              BTGRD +s"╚═════════════════════"             +"═"*(W_WID-35)+"════════════╝"

  /**
   * Печать 1 или нескольких строк шаблона WINDOW_ROW (с переданным текстом)
   *
   * Параметры:
   *  - x: Int Координата x
   *  - y: Int Координата y
   *  - str: String - строка для печати
   */
  val printToRow: (Int, Int, String) => Unit = (x: Int, y: Int, str: String) => {
    val rows = str.split("\n")
    println(pos(1,y) + WINDOW_ROW*rows.length)
    var Y = y
    rows.foreach(s => {println(pos(x,Y)+s);Y+=1})
  }
  /*--------------- Окно Info-----------------------------*/
  /**
   * Шаблон окна WindowInfo (Навигатор по файлам .parquet) (y=5)
   */
  val INFO_FRAME: String = pos(0,5) +
      BTGRD + s"╟"                         +"─"*(W_WID-24)      +s"┬─────────┬───────────╢\n" +
      BTGRD + s"║ ${T_STT}File Name$C_GRD "+" "*(W_WID-35)+BTGRD+s"│  ${T_STT}Size  $BTGRD │ ${T_STT}CountRows $BTGRD"+"║\n"+
              s"╟────────────────"         +"─"*(W_WID-40)      +s"┼─────────┼───────────╢\n"+
              s"║"                         +" "*(W_WID-24)      +s"│         │           ║\n"+Range.inclusive(13, W_HIG).map(_=>
             (s"║"                         +" "*(W_WID-24)      +s"│         │           ║\n")).mkString +
              s"╟"                         +"─"*(W_WID-24)      +s"┴─────────┴───────────╢\n"+
              s"║"                         +" "*(W_WID-2)                              +"║\n"

  /**
   * Шаблон окна WindowConvert (настройки конвертации и запуск конвертации) (y=5)
   */
  val CONVERT_FRAME: Array[String] = ( pos(1, 5) +
           BTGRD + s"╟${"─"*(W_WID/2-14)}${T_STT}CSV conversion parameters$BTGRD"+"─"*(W_WID/2-14)    +"\n"+
           BTGRD + s"║"                                               +" "*(W_WID-2)                 +"║\n"+
           BTGRD + s"║ ${T_STT}Enter the output path:"                +" "*(W_WID-25)+BTGRD          +"║\n" +
           BTGRD + s"║ "                                              +" "*(W_WID-3)+C_GRD+T_GRD     +"║\n" +
           BTGRD + s"║ "                                              +" "*(W_WID-3)+C_GRD+T_GRD     +"║\n" +
           BTGRD + s"║ ${T_STT}Sample size: "                         +" "*(W_WID-17)                + "\n" +
           BTGRD + s"║ "                                              +" "*(W_WID-4)                 +" \n" +
           BTGRD + s"║ ${T_STT}Delimiter:  "                          +" "*(W_WID-15)                +"\n" +
           BTGRD + s"║ "                                              +" "*(W_WID-4)                 +" \n" +
              Range.inclusive(16, W_HIG).map(_=>WINDOW_ROW).mkString )
    .split("\n")
  /**
   * Основной шаблон окна FileInfo (окно подробной информации о файле)
   */
  val FILE_INFO: Array[String] = (
     BGRAY+T_STT +s"╔Schema:"                                            +"═"*(W_WID-11)   +"╗\n"+Range.inclusive(7, W_HIG).map(_=>
      BGRAY+T_STT +s"║ "                                                  +" "*(W_WID-5)    +"║\n").mkString +
      BGRAY+T_STT +s"╚═══"+"═"*((W_WID-10)/2-1)+C_BTN+"[OK]"+BGRAY+T_STT+"═"*((W_WID-10)/2) +"╝")
    .split("\n")

  // Управление буфером восстановления окна
  private var bufferWindow: Array[String] = _

  /**
   * Сохранить текущее состояние окна в буфер
   * @param windowStr строка шаблона окна (с позиции Path)
   */
  def saveDrawnWindow(windowStr: String ): Unit = {bufferWindow = Array(PATH_HEADER) ++ windowStr.split("\n")}
  /**
   * Восстановление. Напечатать сохраненную в буфере строку
   * @param rows количество строк для печати
   */
  def restoreDrawnWindow(rows: Int): Unit = {bufferWindow.take(rows).foreach(println)}

}