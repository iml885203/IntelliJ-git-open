package com.github.iml885203.intellijgitopen.actions

import com.github.iml885203.intellijgitopen.GitHelper
import com.github.iml885203.intellijgitopen.MyNotifier
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.CollectionListModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent

class CreateMergeRequestAction : AnAction() {
    private var lastSearchText: String = ""
    private var currentBranch: String? = null
    private var remoteUrl: String? = null

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return
        if (!GitHelper.isGitRepository(projectPath)) {
            MyNotifier.notifyError(project, "The current project is not a Git repository.")
            return
        }

        val git = Git.open(File(projectPath))
        val branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call()
        val branchNames = branches.map { it.name.substring(it.name.lastIndexOf("/") + 1) }
        currentBranch = git.repository.branch
        remoteUrl = GitHelper.getRemoteUrl(projectPath)

        val list = JBList(CollectionListModel(branchNames))
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.selectedIndex = 0

        val searchField = createSearchField(list, branchNames)
        val panel = createPanel(searchField, list)

        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, searchField)
            .setTitle("Select a remote branch")
            .setResizable(true)
            .setFocusable(true)
            .setRequestFocus(true)
            .createPopup()

        addEnterKeyListener(searchField, list, popup)

        popup.showInFocusCenter()
    }

    private fun createSearchField(list: JBList<String>, branchNames: List<String>): SearchTextField {
        return SearchTextField().apply {
            addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    val text = text.trim()
                    lastSearchText = text
                    val filteredBranchNames = if (text.isEmpty()) {
                        branchNames
                    } else {
                        branchNames.filter { it.contains(text, ignoreCase = true) }
                    }
                    val newModel = CollectionListModel(filteredBranchNames)
                    list.model = newModel
                    if (newModel.size > 0) {
                        list.selectedIndex = 0
                    }
                }
            })
            text = lastSearchText
        }
    }

    private fun createPanel(searchField: SearchTextField, list: JBList<String>): JPanel {
        return JPanel(BorderLayout()).apply {
            preferredSize = Dimension(400, 300)
            add(searchField, BorderLayout.NORTH)
            add(JBScrollPane(list), BorderLayout.CENTER)
        }
    }

    private fun addEnterKeyListener(searchField: SearchTextField, list: JBList<String>, popup: JBPopup) {
        searchField.textEditor.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> {
                        if (list.selectedValue == null) return
                        handleSelectBranch(list.selectedValue)
                        popup.closeOk(null)
                    }
                    KeyEvent.VK_UP -> {
                        val selectedIndex = list.selectedIndex
                        if (selectedIndex > 0) {
                            list.selectedIndex = selectedIndex - 1
                        }
                    }
                    KeyEvent.VK_DOWN -> {
                        val selectedIndex = list.selectedIndex
                        if (selectedIndex < list.model.size - 1) {
                            list.selectedIndex = selectedIndex + 1
                        }
                    }
                }
            }
        })
    }

    private fun handleSelectBranch(selectedBranch: String) {
        BrowserUtil.browse("$remoteUrl/merge_requests/new?merge_request%5Bsource_branch%5D=$currentBranch&merge_request%5Btarget_branch%5D=$selectedBranch")
    }
}