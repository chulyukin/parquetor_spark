package com.proto.parquetor

import com.proto.parquetor.Templates._
//import com.proto.parquetor.Templates.{CONVERT_FRAME, WINDOW_ROW, W_HIG, W_WID}
import org.apache.spark.sql.DataFrame
import org.jline.terminal.Terminal

/**
 * Объект WindowConvert - настройка конвертации и конвертация .pqrquet -> csv
 */
object WindowConvert {
  /**
   * Нарисовать окно WindowConvert
 *
   * @param terminal объект Terminal
   * @param arrFiles Array[(String,Long,Long,String, DataFrame) - массив - структура файлов:
   *                                 - filename имя файла parquet
   *                                 - size - размер файла parquet
   *                                 - count - количество записей в файле
   *                                 - data.schema.treeString - схема файла parquet
   *                                 - data - Spark DataFrame с прочитанным файлом
   */
  def windowConvert(terminal: Terminal, arrFiles: Array[(String, Long, Long, String, DataFrame)]): Unit = {
    print(CONVERT_FRAME.mkString("\n"))
    saveDrawnWindow(CONVERT_FRAME.take(3).mkString("\n"))
    // Значения по умолчанию
    val output_text = "~/output"
    val sampleSize_text = "35"
    val efDelimiter_text = ";"
    // Поля
    val efOutput        = new FieldEdit(terminal,3,8,W_WID-4, output_text) {
                             // Перегрузка метода (Должен вернуть true или false)
                              override def on_Tab( ): Boolean = {if (isTextEmpty) setText(output_text);false}
                              override def on_Enter( ): Boolean = {if (isTextEmpty) setText(output_text);false}
                                                                             }
    val efSampleSize    = new FieldEdit(terminal,16,10,6,sampleSize_text, typeField = TYPE_I){
                              // Перегрузка метода (Должен вернуть true или false)
                              override def on_Tab( ): Boolean = {if (isTextEmpty) setText(sampleSize_text);false}
                              override def on_Enter( ): Boolean = {if (isTextEmpty) setText(sampleSize_text);false}
                                                                              }
    val efDelimiter     = new FieldEdit(terminal,16,12,1,";"){
                              // Перегрузка метода (Должен вернуть true или false)
                              override def on_Tab( ): Boolean = {if (isTextEmpty) setText(efDelimiter_text);false}
                              override def on_Enter( ): Boolean = {if (isTextEmpty) setText(efDelimiter_text);false}
                                  }
    val cbSampleAll     = new CheckBox(terminal,23,10,false, "All Rows", right = true)
    val cbDelimiterTab  = new CheckBox(terminal,23,12,false, "TAB", right = true)
    // Кнопка
    def runConvProc(x:Int,y:Int, delimiter: String = ";", sample: String):Unit = {
      print(pos(2,y) + BRED + " "*(W_WID-2))
      convertToCsv(path = truncPathToReal(efOutput.getText)
        ,parquets = arrFiles
        ,delimeter=delimiter, limitSize=sample
        ,y_pos = y)
      print(pos(x,y) + WINDOW_ROW)
      print(pos(x+2,y) + TWHITE + s"${arrFiles.length} files converted successfully...")
    }

    val btnConvert      = new Button(terminal,16,15, W_WID-16*2,  "Convert to *.csv "){
                            // Перегрузка метода (Должен вернуть true или false)
                            override def on_Esc( ): Boolean = {
                              setState(false)
                              setText("Pressed ESC   ")
                              false}
                            override def on_Tab( ): Boolean = { setState(true); false}
                            override def on_Enter( ): Boolean = {
                                setText(TWHITEBLINK + "Running conversion files...")
                                runConvProc(x=1,y=16
                                             ,delimiter = if (cbDelimiterTab.isChecked) "\t" else efDelimiter.getText
                                             ,sample = if (cbSampleAll.isChecked) "All" else efSampleSize.getText)
                                setState(false)
                                setText("Conversion completed!")
                                false}
                              }


    // Строка состояния
    def statusBar(text: String): Unit = {
      val splitedText = text.split("\n")
      setPos(0,W_HIG-4)
      Range.inclusive(W_HIG-4, W_HIG-1).map(_=> print(WINDOW_ROW) )
      var Y = W_HIG - splitedText.length
      splitedText.foreach(x => {print(pos(3,Y) + BTGRD + x); Y+=1})
    }
    // Переключение между EditField и CheckBox
    def tabEditFieldCheckBox(ef: FieldEdit, cb: CheckBox): Unit = {
      val efStatus = "Press Enter to switch to the next parameter."
      val cbStatus = efStatus + "\n"+ "Press Space to change parameter."
      // Цикл обработки
      var run = true
      var (current,ret) = if (ef.isEnable) ("EF", ef.activate()) else ("CB", cb.activate())
      statusBar(text = if (current=="EF") efStatus else cbStatus)
      while (run) {
        // Выбор активного элемента
        ret match {
          case KEY_TAB =>
            ret = if (current == "CB" & ef.isEnable) { // Если SampleSize -> SampleAll
              current = "EF"
              statusBar(text = efStatus)
              ef.activate()
            }
            else { // Если SampleAll -> SampleSize
              current = "CB"
              statusBar(text = cbStatus)
              cb.activate()
            }
            run = true
          case KEY_SPACE =>
            ret = if (current == "CB") {
              ef.setState(!cb.isChecked)
              cb.activate()
            }
            else KEY_SPACE
          case KEY_ENTER => run =false // выход
          case _ =>  run = true
          //
        }
      }
    }
    // Нарисовать кнопку
    btnConvert.setState(true)


    var key_pressed = 0
    var run = true
    var idx = 0
    while (run) {
      idx match {
        case 0  =>
          statusBar(text = "Press Enter or Tab to switch to the next parameter." )
          efOutput.activate( )
          idx+=1
        case 1 =>
          statusBar(text = "Press Enter to switch to the next parameter.\nPress Tab to switch to the check box." )
          tabEditFieldCheckBox(ef = efSampleSize, cb= cbSampleAll)
          idx+=1
        case 2 =>
          statusBar(text = "Press Enter to switch to the next parameter\nPress Tab to switch to the check box." )
          tabEditFieldCheckBox(ef = efDelimiter, cb= cbDelimiterTab)
          idx+=1
        case 3 =>
          statusBar(text = "Press Enter to start the conversion.\n" +
            "Press Esc to return to the main menu.\n"+
            "Press Tab to return to parameters editing")
          key_pressed = btnConvert.activate()
          key_pressed match {
            case KEY_TAB => idx=0
            case KEY_ENTER => run = false
            case KEY_ESC => run = false
            case _ =>
          }
      }
    }
  }
}
