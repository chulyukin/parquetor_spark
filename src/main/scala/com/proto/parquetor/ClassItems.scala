import com.proto.parquetor.Templates.{BTGRD}
import org.jline.terminal.Terminal

package com.proto.parquetor {

/** trait - состояние компонента (поля, кнопки). bkgColor цвет текста и фона*/
  sealed trait CState {def bkgColor: String}
/** Объект - состояние компонента (поля, checkbox) Enable/Disable */
  object FieldState {
    case object Enable  extends CState {val bkgColor: String = BBLACKL + TGREENLIGHT}
    case object Disable extends CState {val bkgColor: String = BTGRD}
  }
  /** Объект - состояние компонента (кнопки) Enable/Disable */
  object ButtonState {
    case object Enable  extends CState {val bkgColor: String = BGREEL + TWHITE}
    case object Disable extends CState {val bkgColor: String = BGRAYLL + TWHITE}
  }
  /** Класс - поле ввода
   * @param terminal объект Terminal
   * @param x координата X (номер колонки на экране)
   * @param y координата Y (номер строки на экране)
   * @param w ширина поля
   * @param initValue инициирующее значение строки в поле
   * @param typeField Тип поля (TYPE_S, TYPE_I - строка или число)
   * @param enable если true - поле доступно для ввода
   **/
  class FieldEdit(terminal: Terminal
                 ,x: Int, y: Int, w: Int
                 ,initValue: String = ""
                 ,typeField: String = TYPE_S
                 ,enable: Boolean = true)
  {
    // Доступность (активное/неактивное поле)
    private var state: CState = if (enable) FieldState.Enable else FieldState.Disable
    /**
     * Установить состояние поля
     * @param enable true - сделать поле активным (доступным для ввода)
     */
    def setState( enable: Boolean = false ): Unit = {
      state = if (enable) FieldState.Enable else FieldState.Disable
      print(pos(x,y) + state.bkgColor + " "* w)
      draw(false)
    }
    /**
     * Доступно ли поле для ввода
     * @return true - доступно (enabled)
     */
    def isEnable:Boolean =  state == FieldState.Enable

    // Нарисовать текст
    private def draw(visCursor:Boolean = true ): Unit = {
        setPos(x,y, (state == FieldState.Disable)|(!visCursor))
        print(state.bkgColor + text)
        }

    // Текст поля
    private val text = new StringBuilder(initValue)

    /**
     * Записать текст в поле
     * @param t текст
     */
    def setText (t: String): Unit = { // Установить текст
      print(pos(x,y) + " "*text.length)
      text.clear()
      text.insert(0,t)
      draw()
      cursorPos = 0
      setPos(x,y,hide = false)
    }

    /**
     * Возвращает тест поля
     * @return текст поля
     */
    def getText: String = text.toString       // Получить текст

    /**
     * Проверка поля на пустоту
     * @return true - поле пустое
     */
    def isTextEmpty: Boolean =  text.isEmpty  // Проверить пустое ли поле

    private var cursorPos = 0
    setState(enable)
    // Проверка корректности значения
    private def isValidChar(c: Char): Boolean = (typeField==TYPE_S)|(typeField==TYPE_I&c.isDigit)
    // Обработка клавиш
    private def on_Backspace(position: Int ): Int = { // Клавиша Backspace
      if (position >0) {
        setPos(x + text.length-1, y)
        print(state.bkgColor + " ")
        text.delete(position-1,position)
        draw(false)
        setPos( x + position-1,y,hide = false)
        position - 1
      }
      else 0
    }
    private def on_Delete(position: Int ): Int = { // Клавиша Delete
      if (cursorPos < text.length) {
        setPos(x + text.length-1, y)
        print(state.bkgColor + " ")
        text.delete(position, position + 1)
        draw()
        setPos(x + position, y, hide = false)
      }
      position
    }
    /**
     * Нажата клавиша TAB
     * (метод для override)
     * @return false - завершение обработчика клавиш
     */
    def on_Tab( ): Boolean = false // Клавиша TAB
    /**
     * Нажата клавиша Enter
     * (метод для override)
     * @return false - завершение обработчика клавиш
     */
    def on_Enter( ): Boolean = false // Клавиша Enter

    /**
     * Активация поля. Передача управления полю (обработка клавиш)
     * @return код клавиши выхода (константа KEY_*)
     */
    def activate(): Int = {
      setPos(x,y,hide = false)
      cursorPos = 0
      var run = state == FieldState.Enable
      var ret_key = 0
      while (run) {
        val key = terminal.reader().read()
        key match {
          case KEY_BACKSPACE => cursorPos = on_Backspace(cursorPos)
          case KEY_TAB       => run = on_Tab( )
          case KEY_ENTER     => run = on_Enter( )
          case KEY_ESC       =>
            if (terminal.input().read() == 91)
              terminal.input().read() match {
                case KEY_UP     => // Стрелочка вверх
                case KEY_DOWN   => // Стрелочка вниз
                case KEY_RIGHT  => // Стрелочка вправо
                  if (cursorPos < text.length) {
                    cursorPos +=1
                    setPos(x+cursorPos,y,hide = false)}
                case KEY_LEFT   => // Стрелочка влево
                  if (cursorPos >0) {
                    cursorPos -=1
                    setPos(x+cursorPos,y,hide = false)}
                case KEY_DELETE => // Delete
                  terminal.input().read() // задавить 3ий byte delete
                  cursorPos = on_Delete(cursorPos)
                case KEY_INSERT => // Insert
                case KEY_HOME => // Home
                  cursorPos = 0
                  setPos(x + cursorPos, y, hide = false)
                case KEY_END => // End
                  cursorPos = text.length
                  setPos(x + cursorPos, y, hide = false)
                case KEY_PGUP => // PageUp
                case KEY_PGDOWN => // PageDown
                case _ =>
              }
          case -1 =>
          case _ => // Char keys
            if (cursorPos < w &isValidChar(key.toChar)) {
              setPos(x + cursorPos, y)

              print(state.bkgColor + key.toChar)
              text.delete(cursorPos, cursorPos + 1)
              text.insert(cursorPos, key.toChar)

              if (cursorPos >= w-1) cursorPos = w - 1 else cursorPos += 1

              setPos(x + cursorPos, y, hide = false)
            }
        }
        ret_key = key // Управляющие клавиши имеют код KEY_ESC
      }
      ret_key
    }
  }

  /**
   * Класс CheckBox - поле выбора
   * @param terminal объект Terminal
   * @param x координата X (номер колонки на экране)
   * @param y координата Y (номер строки на экране)
   * @param checked инициирующее значение (true - [X], false - [ ])
   * @param stext статический текст-описание
   * @param right напечатать sname (true - sname справа, false - sname слева)
   * @param enable если true - [X] доступно для изменения
  */
  class CheckBox(terminal: Terminal,x: Int, y: Int
                 ,checked: Boolean = false
                 ,stext: String ="", right: Boolean = false
                 ,enable: Boolean = true  ) {
    // Доступность (активное/неактивное поле)
    private var state: CState = if (enable) FieldState.Enable else FieldState.Disable
    // enable = true - сделать поле активным
    /**
     * Установить состояние checkbox
     * @param enable true - сделать checkbox активным (доступной для изменения)
     */
    def setState( enable: Boolean = false ): Unit = {
      state = if (enable) FieldState.Enable else FieldState.Disable
      draw()
    }
    // Если активно, то true

    /**
     * Доступен ли checkbox для изменения
     * @return true - доступен (enabled)
     */
    def isEnable:Boolean =  state == FieldState.Enable

    // значение поля
    private var value: Boolean = checked

    /**
     * Отмечен ли checkbox
     * @return true - [X], false - [ ]
     */
    def isChecked:Boolean = value

    // Координата checkBox X
    private val X = x + (if (!right) stext.length + 2 else 1)
    // Нарисовать checkbox
    private def draw( ): Unit = {
      val valueX = if (value) "X" else " "
      print(pos(x, y) + state.bkgColor + (if (!right) stext + s" [$valueX]" else s"[$valueX] " + stext + BTGRD))
    }
    // Выбрать checkbox value

    /**
     * Изменить состояние checkbox, ([X]->[ ] - [ ]->[X]).
     * Каждый вызов меняет состояние на противоположное
     */
    def check( ): Unit = {
      print(pos(X, y) + state.bkgColor + (if (isChecked) s" " else s"X") + pos(X, y))
      value = !isChecked}

    // Нарисовать
    draw()
    /**
     * Нажата клавиша TAB
     * (метод для override)
     * @return false - завершение обработчика клавиш
     */
    def on_Tab( ): Boolean = false // Клавиша TAB
    /**
     * Нажата клавиша Enter
     * (метод для override)
     * @return false - завершение обработчика клавиш
     */
    def on_Enter( ): Boolean = false // Клавиша Enter

    /**
     * Активация кнопки. Передача управления кнопке (обработка клавиш)
     * @return код клавиши выхода (константа KEY_*)
     */
    def activate(): Int = {
      setPos(x+1, y, hide = false)
      var run = true
      var ret_key = 0
      while (run) {
        val key =terminal.reader().read()
        key match {
          case KEY_SPACE => check(); run = false
          case KEY_ENTER => run = on_Enter()
          case KEY_TAB   => run = on_Tab( )
          case _ =>
        }
        ret_key = key
      }
      ret_key
    }
  } //CheckBox

  /**
   *  Класс - кнопка.
   *  @param terminal объект Terminal
   *  @param x координата X (номер колонки на экране)
   *  @param y координата Y (номер строки на экране)
   *  @param w ширина кнопки.
   *  @param text String  Текст кнопки.
   *  @param enable если true - кнопка доступна для нажатия
   */
  class Button(terminal: Terminal,x: Int, y: Int, w: Int, text:String, enable: Boolean = true) {
    // Доступность (активное/неактивное поле)
    private var state: CState = if (enable) ButtonState.Enable else ButtonState.Disable

    /**
     * Установить состояние кнопки
     * @param enable true - сделать кнопку активной (доступной для нажатия)
     */
    def setState( enable: Boolean = false ): Unit = {
      state = if (enable) ButtonState.Enable else ButtonState.Disable
      print(pos(x,y) + state.bkgColor + " "* w)
      draw()
    }
    /**
     * Доступна ли кнопка для нажатия.
     * @return true - кнопка доступна (enabled)
     */
    def isEnable: Boolean =  state == ButtonState.Enable

    // Нарисовать кнопку
    private def draw( ): Unit = {
      val x_text = (w-text.length)/2
      setPos(x+x_text,y)
      print(state.bkgColor + text)
    }

    /**
     *  Установить текст кнопки
     * @param t текст
     * @return true
     */
    def setText (t: String): Boolean = { // Установить текст
      val x_text = (w-t.length)/2
      print( pos(x,y) + state.bkgColor + " "* w + pos(x+x_text,y) + t)
      true}

    /**
     * Нажата клавиша ESC
     * (метод для override)
     * @return true - завершение обработчика клавиш
     */
    def on_Esc (): Boolean =  true
    /**
     * Нажата клавиша TAB
     * (метод для override)
     * @return true - завершение обработчика клавиш
     */
    def on_Tab (): Boolean = true
    /**
     * Нажата клавиша Enter
     * (метод для override)
     * @return true - завершение обработчика клавиш
     */
    def on_Enter (): Boolean = true

    /**
     * Активация кнопки. Передача управления кнопке (обработка клавиш)
     * @return код клавиши выхода (константа KEY_*)
     */
    def activate(): Int = {
      print(pos(x - 1, y) + TWHITE + "[")
      print(pos(x + w, y) + TWHITE + "]")
      var key_ret = 0
      var run: Boolean = true
      while (run) {
        val key = terminal.input().read()
        key match {
          case KEY_ESC =>
            terminal.reader().read(100) match {
              case -2 =>
                key_ret = KEY_ESC
                run = on_Esc()
              case _ =>
            }
          case KEY_TAB   =>  run = on_Tab()
          case KEY_ENTER =>  run = on_Enter()
          case _         =>  run = true
        }
        key_ret = key
      }
      print(pos(x - 1, y) + BTGRD + " ")
      print(pos(x + w, y) + BTGRD + " ")
      key_ret
    }
  }

} //package