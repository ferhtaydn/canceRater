package com.ferhtaydn.settings

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {
  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")
  val corpus = config.getString("ml.corpus.path")
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  override def lookup = Settings
  override def createExtension(system: ExtendedActorSystem) = new SettingsImpl(system.settings.config)
}
