package com.proto.parquetor

import com.proto.parquetor.Templates.{C_GRD, C_MNU, W_WID}

/**
 * Объект Главное меню
 */
object Menu {
  /**
   * Пункты меню
   */
  val menuItems: Array[String] = Array("Info", "Convert", "Exit")

  // Нарисовать Меню x=12, y = 2
  /**
   *  Нарисовать Меню координатам x=12, y=2
   *
   *  Параметры
   *  - current: Int - текущий выбор меню (цвет)
   *  - select: Int - новый выбор меню ([ ])
   */
  val MenuBar: (Int, Int) => Unit = (current: Int, select: Int ) => {
    setPos(12,2) // Меню во второй строке
    val IDENT = " "*((W_WID-5)/8) // Отступы между пунктами
    // Выбор меню
    val menu: Array[String] = menuItems.zipWithIndex.map{ case (value, index) =>
      if (index == current&index==select) s"[$C_MNU$value$C_GRD]"
      else if (index == current&index!=select) s" $C_MNU$value$C_GRD "
      else if (index!=current&index==select) s"[$C_GRD$value$C_GRD]"
      else s" $C_GRD$value$C_GRD "}
    print("\u001b[?25l")
    // Нарисовать меню
    println(C_GRD + menu.mkString(IDENT))
    System.out.flush()
  }

}
