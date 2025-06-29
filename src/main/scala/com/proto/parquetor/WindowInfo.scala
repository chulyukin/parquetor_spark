package com.proto.parquetor

import org.jline.terminal.Terminal
import com.proto.parquetor.Templates._
import org.apache.spark.sql.DataFrame

/** Объект WindowInfo - навигатор файлов .parquet */
object WindowInfo {
  /* Модальное окно вывода схемы файла.
  f_name: String - Имя файла
  schema: String - Схема
  */
  private def fileInfoWindow(terminal:Terminal,f_name: String, schema: String): Unit = {
    val X = 2; val Y = 4 // 4 строка 2а позиция ()
    val ELEMENT_CNT = W_HIG - 6 // Количество отображаемых элементов схемы
    // Структура - schema
    val arrSchema = schema split "\n" drop 1  // Удалить заголовок
    val count = arrSchema.length
    // Нарисовать рамку окна и напечатать имя файла (после надписи Schema)
    FILE_INFO.zipWithIndex foreach {case(x,i) => println(pos(X,Y+i) + x)}
    println(pos(X+8,Y)+ " " + BGRAY+f_name + " ")

    // Нарисовать схему
    def printSchema(startRow:Int): Unit = {
      arrSchema.slice(startRow, count).take(ELEMENT_CNT).zipWithIndex.foreach {
          case(x,i) => println(pos(X+2,(Y+1)+i) + BGRAY + x + T_STT + T_GRD + " " * (W_WID - x.length - 5))
              }
      // Нарисовать стрелочки - позиция - 3 (2ая строка снизу)
      if(count>ELEMENT_CNT) {
        setPos(3,W_HIG-1)
        if (startRow==0) { // Вначале списка
          print(s"$BORANGE[↓]$BGRAY$T_STT═")
          print(pos(W_WID-4,W_HIG-1) + s"$BGRAY$T_STT═══")
        }
        else if (startRow+ELEMENT_CNT<count) { // В середине списка
          print(s"$BORANGE[↓]$BGRAY$T_STT═")
          setPos(W_WID-4,W_HIG-1)
          print(s"$BORANGE[↑]")
        }
        else {
          print(BGRAY+T_STT + s"═══") // В конце списка
          setPos(W_WID-4,W_HIG-1)
          print(s"$BORANGE[↑]")
        }
      }
    }
    // Обработка клавиш
    var run = true
    var minRow = 0
    while (run) {
      printSchema(minRow)
      val key = terminal.input().read()
      key match {
        case KEY_ESC => // ESC - клавши
          // Стрелочки
          if (terminal.input().read() == 91) // ESC - клавиши
            terminal.input().read() match {
              case KEY_UP     => if (minRow > 0) minRow -= 1 // Вверх
              case KEY_DOWN   => if (minRow+ELEMENT_CNT<count) minRow += 1 // Вниз
              case KEY_PGUP   => if (minRow > 5) minRow -= 5 else minRow = 0 // PageUp
              case KEY_PGDOWN => if (minRow+ELEMENT_CNT<count-5) minRow += 5 // PageDown
              case KEY_HOME   => minRow = 0
              case KEY_END    => minRow = count-ELEMENT_CNT
              case KEY_INSERT =>
              case KEY_DELETE =>
              case _ => run = false
            }
        case KEY_ENTER => run = false // выход в основное окно
        case _ => run = true
      }
    }

  }
  /* Окно списка файлов
  arrFiles: Array - (Имя файла, Размер (Byte), Кол-во строк, Схема, DataFrame)
  */

  /**
   * Окно списка файлов
   * @param terminal объект Terminal
   * @param arrFiles Array[(String,Long,Long,String, DataFrame) - массив - структура файлов:
   *                                                  - filename имя файла parquet
   *                                                  - size - размер файла parquet
   *                                                  - count - количество записей в файле
   *                                                  - data.schema.treeString - схема файла parquet
   *                                                  - data - Spark DataFrame с прочитанным файлом
   */
  def windowInfo(terminal:Terminal,arrFiles: Array[(String, Long, Long, String, DataFrame)]): Unit = {
    val BG = C_GRD + T_GRD // Фон
    // Количество файлов
    val f_count = arrFiles.length
    val totalFiles = f_count.toString
    val totalSize = bytesHR(arrFiles.map(_._2).sum) // Суммарный объем
    val F_VIEW = math.min(W_HIG - 11,f_count) // Количество файлов на экране
    // Подпись
    val footer_prompt = C_GRD +(if (f_count > F_VIEW) s"More then $F_VIEW elements found." else "All elements printed.") +BG +"\n"

    /* Напечатать файлы
      startRow: Int - стартовая позиция
      select: Int   - индекс выбранного файла
    */
    def printFiles(startRow: Int, select: Int): Unit = {
      val files = arrFiles.slice(startRow, startRow + F_VIEW)
      files.take(F_VIEW).zipWithIndex.foreach {
        case ((name, size, count, _, _),i) =>
          //Форматирование отображаемой длины
          val f_name = if (name.length > (W_WID - 30)) name.substring(0, W_WID - 33) + "..." else name
          val f_size = bytesHR(size)
          // Печать файла
          print(pos(2,i+8) +BG+C_GRD +(if (select == i+1) s"$C_MNU[$f_name]" else s" $f_name " ) +BG +" "*(W_WID-28-f_name.length) )
          // Печать параметров файла
          setPos(W_WID - 21, i + 8)
          println(s" $C_GRD${" " * (7 -f_size.length) + f_size} $BG│ " + C_GRD + " " * (9 -count.toString.length) + count +" " +BG)
      }
    }
    def printFooter(): Unit = {printToRow(3, W_HIG-2, footer_prompt +s"${T_STT}Total Files:$C_GRD $totalFiles  ${T_STT}Total Size: $C_GRD$totalSize") }

    saveDrawnWindow(INFO_FRAME)
    print(INFO_FRAME)
    printFiles(0, 0)
    printFooter()

    val restore = () => {
      print(PATH_HEADER)
      print(INFO_FRAME)
      printFooter()
    }
    // Обработчик
    var pressed_enter = true; var run = true; var minRow = 0
    //Выбранный файл
    var sel_i = 1
    while (run) {
      printFiles(minRow, sel_i)
      val key = terminal.input().read()
      key match {
        // ESC - клавши
        case KEY_ESC =>
          // Стрелочки
          if (terminal.input().read() == 91)
            terminal.input().read() match {
              // Стрелочка вверх
              case KEY_UP =>
                sel_i -= 1
                if (minRow == 0 & sel_i <= 1) sel_i = 1 // вызвать 0 0
                else if (sel_i < 1) {minRow -= 1; sel_i = 1 } // вызвать minRow-1 selIdx =0
                run = true
              // Стрелочка вниз
              case KEY_DOWN =>
                run = true
                sel_i += 1
                if (minRow + F_VIEW < f_count & sel_i > F_VIEW) {minRow += 1; sel_i = F_VIEW}
                else if (minRow + F_VIEW == f_count & sel_i > F_VIEW) sel_i = F_VIEW
              case KEY_LEFT => run = false  // Стрелочка вправо
              case KEY_RIGHT => run = false // Стрелочка влево
              case _ =>
            }
        // ENTER - FILE_INFO
        case KEY_ENTER =>
          val fileName = arrFiles(minRow + sel_i - 1)
          fileInfoWindow(terminal,fileName._1, fileName._4)
          restore()
          pressed_enter = true
        case KEY_TAB => run = false
        case _ =>
      }
    }
  }
}