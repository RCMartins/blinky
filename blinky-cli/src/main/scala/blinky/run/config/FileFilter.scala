package blinky.run.config

import os.RelPath

sealed trait FileFilter

object FileFilter {

  case class SingleFileOrFolder(fileOrFolder: RelPath) extends FileFilter

  case class FileName(fileName: String) extends FileFilter

}
