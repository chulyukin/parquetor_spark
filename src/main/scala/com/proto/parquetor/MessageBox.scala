package com.proto.parquetor

import com.proto.parquetor.Templates.{C_BTN, C_GRD, W_WID,T_GRD}
import org.jline.terminal.Terminal

/**
 * Объект окон сообщений
 */
object MessageBox {
  // Конфигурация кнопок message box
  final val MB_OK:    Int = 0 // Кнопка OK
  final val MB_YESNO: Int = 1 // Кнопки Yes No

  /**
   * Окно message box
   * @param terminal Instance  Terminal
   * @param message Строка - сообщение
   * @param header Строка - заголовок
   * @param buttons Конфигурация кнопок (MB_YESNO - Yes No, MB_OK - Ok)
   * @param backgound   Цвет окна
   * @return MB_YESNO (Yes - true, No - False), MB_OK (Ok - true)
   */
  def messageBox(terminal: Terminal
                 ,message: String, header: String = ""
                 ,buttons: Int = MB_YESNO, backgound: String = BRED
                ): Boolean = {
    val X = W_WID/4 - 3; val Y = 5   // Координаты box
    val boxlen = W_WID - 2*X - 2     // Ширина box
    val text = message.substring(0, Seq(boxlen-2, message.length).min)
    // Тогда Yes/No будут:
    val buttinX = X + (buttons match {
      case MB_YESNO  => boxlen/2-5
      case MB_OK => (boxlen-"OK".length/2)/2
      case _ => -1}
      )
    val tablen = buttons match { // две кнопки
      case MB_OK => 1
      case MB_YESNO => 2
    }
    // Кнопки Yes/No
    def yesno(select: Int): String = {
      setPos(buttinX, Y + 2)
      val box = if (select == 0) s"$backgound[${C_BTN}Yes$backgound]══ No "
      else             s"$backgound Yes ══[${C_BTN}No$backgound]"
      println(box)
      box
    }
    // Кнопка ok
    def ok(): String = {
      setPos(buttinX,Y+2)
      val box =  s"$backgound[${C_BTN}OK$backgound]"
      println(box)
      box}
    val txtlen = text.length
    println(C_GRD + T_GRD +
      s"\u001b[${Y+0};${X}H$backgound╔═"     + "═"*(boxlen-header.length)+header    +s"═╗$C_GRD"+
      s"\u001b[${Y+1};${X}H$backgound║ $text"+" "*(boxlen-txtlen)                   +s" ║$C_GRD"+
      s"\u001b[${Y+2};${X}H$backgound╚═"     + "═"*boxlen                           +s"═╝$C_GRD"
    )
    // Обработчик
    var selectIdx = 0
    var run = true
    // Обработчик
    while (run) {
      buttons  match { // две кнопки
        case MB_OK => ok()
        case MB_YESNO => yesno(selectIdx)
      }
      val key = terminal.input().read()
      key match {
        case KEY_TAB => selectIdx = (selectIdx + 1) % tablen
        case KEY_ENTER => run = false
        case _ =>
      }
    }
    // Возвращаемое значение
    selectIdx == 0
  }

}
