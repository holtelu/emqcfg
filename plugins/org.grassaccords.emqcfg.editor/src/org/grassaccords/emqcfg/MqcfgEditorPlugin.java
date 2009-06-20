package org.grassaccords.emqcfg;

import org.openarchitectureware.xtext.AbstractXtextEditorPlugin;
import org.openarchitectureware.xtext.LanguageUtilities;
import org.osgi.framework.BundleContext;

public class MqcfgEditorPlugin extends AbstractXtextEditorPlugin {
   private static MqcfgEditorPlugin plugin;
   public static MqcfgEditorPlugin getDefault() {
      return plugin;
   }

   private MqcfgUtilities utilities = new MqcfgUtilities();
   public LanguageUtilities getUtilities() {
      return utilities;
   }

   public MqcfgEditorPlugin() {
      plugin = this;
   }

   public void stop(BundleContext context) throws Exception {
      super.stop(context);
      plugin = null;
   }
}
