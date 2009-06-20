package org.grassaccords.emqcfg.editor;

import org.openarchitectureware.xtext.AbstractXtextEditorPlugin;
import org.openarchitectureware.xtext.editor.AbstractXtextEditor;

import org.grassaccords.emqcfg.MqcfgEditorPlugin;

public class MqcfgEditor extends AbstractXtextEditor {

   public AbstractXtextEditorPlugin getPlugin() {
      return MqcfgEditorPlugin.getDefault();
   }
}
