/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.debugger.ui.tree.render

import com.intellij.debugger.DebuggerBundle
import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.actions.ArrayAction
import com.intellij.debugger.actions.ArrayFilterAction
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.icons.AllIcons
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.impl.XDebuggerUtilImpl
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreeInplaceEditor
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import java.awt.Rectangle

/**
 * @author egor
 */
class ArrayFilterInplaceEditor(node: XDebuggerTreeNode, val myTemp : Boolean) : XDebuggerTreeInplaceEditor(node, "arrayFilter") {
  override fun cancelEditing() {
    super.cancelEditing()
    if (myTemp) (myNode.parent as XValueNodeImpl).removeTemporaryEditorNode(myNode)
  }

  override fun doOKAction() {
    ArrayAction.setArrayRenderer(if (XDebuggerUtilImpl.isEmptyExpression(expression))
                                   NodeRendererSettings.getInstance().arrayRenderer
                                 else
                                   ArrayRenderer.Filtered(expression),
                                 myNode.parent as XValueNodeImpl,
                                 DebuggerManagerEx.getInstanceEx(project).context)
    super.doOKAction()
  }

  override fun getEditorBounds(): Rectangle? {
    val bounds = super.getEditorBounds() ?: return null

    val nameLabel = SimpleColoredComponent()
    nameLabel.ipad.right = 0
    nameLabel.ipad.left = 0
    nameLabel.icon = myNode.icon
    nameLabel.append(DebuggerBundle.message("message.node.filtered"), SimpleTextAttributes.REGULAR_ATTRIBUTES)
    val offset = nameLabel.preferredSize.width

    bounds.x += offset
    bounds.width -= offset
    return bounds
  }

  companion object {
    @JvmStatic
    fun edit(parentNode: XValueNodeImpl) {
      var temp = false
      var node = parentNode.children.find { ArrayFilterAction.isArrayFilter(it) }
      if (node == null) {
        node = parentNode.addTemporaryEditorNode(AllIcons.General.Filter, DebuggerBundle.message("message.node.filtered"))
        temp = true
      }
      DebuggerUIUtil.invokeLater({ArrayFilterInplaceEditor(node as XDebuggerTreeNode, temp).show()})
    }
  }
}
