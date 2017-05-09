/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * ObjectViewer.java
 *
 * Created on April 17, 2007, 12:46 PM
 */

package Util.GUI;

import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A GUI class designed to display and edit public fields in serialized object
 * files.
 * @author pkv
 */
public class ObjectViewer extends javax.swing.JFrame {
    // This filter is not supported in both Unix and Windows, 
    // but this is what we *would* use.
    private static final String[] FILTER_NAMES = {
      "Serialized Belief Files (*.blf)",
      "XML Belief Files (*.xml)",
      "All Files (*.*)"};
    private static final String[] FILTER_EXTS = { "*.blf", "*.xml", "*.*"};
    
    private Object[] currObjects;
    private String baseTitle;
    private File currFile;
    
    /**
     * Creates new form ObjectViewer
     */
    public ObjectViewer() {
        initComponents();
        baseTitle = this.getTitle();
        resetObjects();
        updateObjects();
        updateObjectPanel(null);
    }
    
    /**
     * Resets to having no file open
     */
    private void resetObjects() {
        this.setTitle(baseTitle);
        currObjects = null;
        currFile = null;
    }
    
    /**
     * Loads the current serialized object file for display in the frame.
     */
    private void loadObjects() {
        // Set this as current file 
        this.setTitle(baseTitle + " - " + currFile.getName());
        
        // Deserialize beliefs from file
        currObjects = readObjects(currFile.getPath());
    }
    
    /**
     * Saves the current object array in serialized form to the current file name.
     */
    private void saveObjects() {
        // If we don't have a file selected, do nothing
        if (currFile == null)
            return;
        
        // Reset title to reflect synchronization with file
        this.setTitle(baseTitle + " - " + currFile.getName());
        
        // Write out to file
        writeObjects(currFile.getPath(), currObjects);
    }
    
    /**
     * Opens specified file and reads out serialized objects until EOF or an 
     * error occurs, and returns these objects in array form.
     * @param fileName A string containing the path to the file that will be read.
     * @return An array of objects deserialized from the file.
     */
    public static Object[] readObjects(String fileName) {
        ArrayList objectList = new ArrayList();
        
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            Object o = null;
            
            do {                   
                try {
                    o = ois.readObject();           
                } catch (EOFException ex) {
                    o = null;
                } catch (ClassNotFoundException ex) {
                    System.err.println("Failed to read serialized object: " + ex);
                    o = null;
                } catch (IOException ex) {
                    System.err.println("Failed to read serialized object: " + ex);
                    o = null;
                }
                if (o != null) {
                    objectList.add(o);
                } else if (o != null) {
                    System.err.println("Deserialized something other than belief: " + o.getClass() + " " + o);
                }
            } while (o != null);
            
            ois.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Could not load serialized beliefs: " + ex);
        } catch (IOException ex) {
            System.err.println("Could not load serialized beliefs: " + ex);
        }
        
        return objectList.toArray();
    }
    
    /**
     * Opens specified file and writes out serialized objects until the given
     * array is completely written to file or an error occurs.
     * @param fileName A string containing the path to the file that will be written.
     * @param objectArray An array of objects that will be serialized to the file.
     */
    public static void writeObjects(String fileName, Object[] objectArray) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            for (int i = 0; i < objectArray.length; i++) {
                Object o = objectArray[i];
                if (o != null) {
                    oos.writeObject(o);
                }
            }
            oos.flush();
            oos.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Problem writing beliefs: " + ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Problem writing beliefs: " + ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Reloads the current object array into tree form, adding loading tags on
     * unexpanded branches.
     */
    private void updateObjects() {        
        // If nothing is loaded, make a trivial tree
        if (currObjects == null) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode( 
                "[No file loaded]" );
            jBeliefTree.setModel(new DefaultTreeModel(root));
        }
        // Repopulate based on current belief array 
        else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode( 
                    currFile.getName() );

            for (int i = 0; i < currObjects.length; i++) {
                if (currObjects[i] != null) {
                    TupleMutableTreeNode objectNode = new TupleMutableTreeNode( 
                            currObjects[i].toString(), currObjects[i] );
                    root.add(objectNode);
                    
                    // Add a placeholder to make the node expandable 
                    // (We assume the node is collapsed by default) 
                    DefaultMutableTreeNode subNode = new DefaultMutableTreeNode("Loading...");        
                    objectNode.add(subNode);
                }
            }
            jBeliefTree.setModel(new DefaultTreeModel(root));
        }
    }
    
    private static class TupleMutableTreeNode extends DefaultMutableTreeNode {
        private Object obj;
        
        public TupleMutableTreeNode(String s, Object o) {
            super(s);
            this.obj = o;
        }
        
        public Object getObject() {return obj;}
        public void setObject(Object o) {obj = o;}
    }
    
    /**
     * Dynamically adds new subnodes to an unexpanded tree node.  Each subnode
     * corresponds to a public field in the object represented by the tree node.
     * @param obj The object represented by a tree node.
     * @param objNode A tree node in the current object array.
     */
    private static void addFieldsToTree(Object obj, TupleMutableTreeNode objNode) {
        if (obj == null)
            return;
        
        // Reflect the class info about this object
        Class c = obj.getClass();
        
        // Iterate through subfields
        Field[] publicFields = c.getFields();
        for (int i = 0; i < publicFields.length; i++) {
            
            // Get reflexive information about the field 
            Object fieldObj = null;
            String fieldName = publicFields[i].getName();
            Class fieldClass = publicFields[i].getType();
            String fieldType = fieldClass.getName();
            try {
                fieldObj = publicFields[i].get(obj);
            } catch (IllegalAccessException ex) {}
            
            // Add the field we found to the tree
            TupleMutableTreeNode fieldNode = new TupleMutableTreeNode(
                    fieldName +
                    ", Type: " + fieldType +
                    ", Value: " + fieldObj,
                    fieldObj );
            objNode.add(fieldNode);
            
            if (fieldClass.isPrimitive()) {
                // Don't allow expansion 
            }
            else if (fieldObj instanceof String) {
                // Don't allow expansion 
            }
            else if ((fieldObj instanceof Map) ||
                     (fieldObj instanceof Collection) ||
                     (fieldClass.isArray())){
                // Add a placeholder to make the node expandable
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode("Loading...");        
                fieldNode.add(subNode);
            }
        }
        
        // Determine if object is a Collection or Array or Set
        Object[] objArr = null;
        if (obj instanceof Map) {
            objArr = ((Map)obj).entrySet().toArray();
        }
        else if (obj instanceof Collection) {
            objArr = ((Collection)obj).toArray();
        }
        else if (c.isArray()) {
            objArr = (Object[])obj;
        }
        else {
            return;
        }
        
        // If object can be enumerated, list elements
        for (int i = 0; i < Array.getLength(objArr); i++) {
            Object currElement = Array.get(objArr, i);
            TupleMutableTreeNode elementNode = new TupleMutableTreeNode(
                "[" + i + "]: " + currElement, currElement);
            objNode.add(elementNode);
            
            // Add a placeholder to make the node expandable
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode("Loading...");        
            elementNode.add(subNode);
        }
    }
    
    /**
     * Updates the object panel display with the information of the specified object 
     * in the tree.
     * @param objNode The object to be displayed in the panel.
     */
    private void updateObjectPanel(TupleMutableTreeNode objNode) {
        if (objNode == null) {
            jNameTextPane.setText("");
            jClassTextPane.setText("");
            return;
        }
        
        jNameTextPane.setText(objNode.getUserObject().toString());
        if (objNode.getObject() != null)
            jClassTextPane.setText(objNode.getObject().getClass().getName());
        else
            jClassTextPane.setText("null");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jSplitPane = new javax.swing.JSplitPane();
        jBeliefScrollPane = new javax.swing.JScrollPane();
        jBeliefTree = new javax.swing.JTree();
        jBeliefPanel = new javax.swing.JPanel();
        jNameScrollPane = new javax.swing.JScrollPane();
        jNameTextPane = new javax.swing.JTextPane();
        jClassScrollPane = new javax.swing.JScrollPane();
        jClassTextPane = new javax.swing.JTextPane();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuBar = new javax.swing.JMenuBar();
        jFileMenu = new javax.swing.JMenu();
        jLoadMenuItem = new javax.swing.JMenuItem();
        jSaveMenuItem = new javax.swing.JMenuItem();
        jSaveAsMenuItem = new javax.swing.JMenuItem();
        jCloseMenuItem = new javax.swing.JMenuItem();
        jFileSeparator = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Machinetta Object Viewer");
        jSplitPane.setDividerLocation(200);
        jBeliefTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jBeliefTreeValueChanged(evt);
            }
        });
        jBeliefTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                jBeliefTreeTreeWillCollapse(evt);
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                jBeliefTreeTreeWillExpand(evt);
            }
        });

        jBeliefScrollPane.setViewportView(jBeliefTree);

        jSplitPane.setLeftComponent(jBeliefScrollPane);

        jBeliefPanel.setFont(new java.awt.Font("Tahoma", 3, 11));
        jNameTextPane.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        jNameTextPane.setEditable(false);
        jNameTextPane.setFont(new java.awt.Font("Tahoma", 1, 11));
        jNameTextPane.setFocusable(false);
        jNameScrollPane.setViewportView(jNameTextPane);

        jClassTextPane.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        jClassTextPane.setEditable(false);
        jClassTextPane.setFocusable(false);
        jClassScrollPane.setViewportView(jClassTextPane);

        org.jdesktop.layout.GroupLayout jBeliefPanelLayout = new org.jdesktop.layout.GroupLayout(jBeliefPanel);
        jBeliefPanel.setLayout(jBeliefPanelLayout);
        jBeliefPanelLayout.setHorizontalGroup(
            jBeliefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jBeliefPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jBeliefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jClassScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .add(jNameScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                .addContainerGap())
        );
        jBeliefPanelLayout.setVerticalGroup(
            jBeliefPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jBeliefPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jNameScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jClassScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(215, Short.MAX_VALUE))
        );
        jSplitPane.setRightComponent(jBeliefPanel);

        jFileMenu.setText("File");
        jFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileMenuActionPerformed(evt);
            }
        });

        jLoadMenuItem.setText("Load...");
        jLoadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLoadMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jLoadMenuItem);

        jSaveMenuItem.setText("Save");
        jSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jSaveMenuItem);

        jSaveAsMenuItem.setText("Save As...");
        jSaveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveAsMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jSaveAsMenuItem);

        jCloseMenuItem.setText("Close");
        jCloseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jCloseMenuItem);

        jFileMenu.add(jFileSeparator);

        jMenuBar.add(jFileMenu);

        setJMenuBar(jMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCloseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseMenuItemActionPerformed
        // Close current file and open blank file
        resetObjects();
        updateObjects();
        updateObjectPanel(null);
    }//GEN-LAST:event_jCloseMenuItemActionPerformed

    private void jBeliefTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jBeliefTreeValueChanged
        // Update the belief panel to match the selection here
        Object eventObj = evt.getPath().getLastPathComponent();
        if (!(eventObj instanceof TupleMutableTreeNode)) {
            return;
        }
        
        TupleMutableTreeNode eventNode = (TupleMutableTreeNode)eventObj;
        updateObjectPanel(eventNode);
    }//GEN-LAST:event_jBeliefTreeValueChanged

    private void jBeliefTreeTreeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jBeliefTreeTreeWillCollapse
        // Check if this node needs dynamic population
        Object eventObj = evt.getPath().getLastPathComponent();
        if (!(eventObj instanceof TupleMutableTreeNode)) {
            return;
        }
        
        // When the tree is collapsed, we delete all the child nodes
        TupleMutableTreeNode eventNode = (TupleMutableTreeNode)eventObj;
        eventNode.removeAllChildren();
        
        // Add a placeholder to make the node expandable
        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode("Loading...");        
        eventNode.add(subNode);
        
        // Trigger a tree update event 
        ((DefaultTreeModel)((JTree)evt.getSource()).getModel()).nodeStructureChanged(eventNode);
    }//GEN-LAST:event_jBeliefTreeTreeWillCollapse

    private void jBeliefTreeTreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jBeliefTreeTreeWillExpand
        // Check if this node needs dynamic population
        Object eventObj = evt.getPath().getLastPathComponent();
        if (!(eventObj instanceof TupleMutableTreeNode)) {
            return;
        }
        
        // When the tree is expanded, we populate all the child nodes 
        TupleMutableTreeNode eventNode = (TupleMutableTreeNode)eventObj;
        eventNode.removeAllChildren();
        
        // Repopulate the node with corresponding object fields 
        Object o = eventNode.getObject();
        addFieldsToTree(o, eventNode);
        
        // Trigger a tree update event
        ((DefaultTreeModel)((JTree)evt.getSource()).getModel()).nodeStructureChanged(eventNode);
    }//GEN-LAST:event_jBeliefTreeTreeWillExpand

    private void jSaveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveAsMenuItemActionPerformed
        final JFileChooser fc;
        
        // If we don't have a file open, do nothing
        if (currFile == null)
            return;

        // Create the file dialog, using current directory if we have one 
        if (currFile.getParent() != null)
            fc = new JFileChooser(currFile.getParent());
        else
            fc = new JFileChooser(".\\");

        // Keep asking the user for a file until they give us one or cancel 
        do {
            // Display file dialog, wait for user response
            int returnVal = fc.showSaveDialog(ObjectViewer.this);

            // If the user cancels, abort function
            if (returnVal != JFileChooser.APPROVE_OPTION)
                return;
        
            // If the file does not exist, quit asking for a file
            if (!fc.getSelectedFile().exists())
                break;
            
            // If the file does not exist, prompt the user for confirmation 
            int confirmVal = JOptionPane.showInternalConfirmDialog(ObjectViewer.this, 
                    "File already exists.  Overwrite?", "Save As", 
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
            
            // If the user accepts, quit asking for a file
            if (confirmVal == JOptionPane.YES_OPTION)
                break;
            // If the user cancels, abort function 
            else if (confirmVal == JOptionPane.CANCEL_OPTION)
                return;
                
        } while (true); //TODO: Put something more elegant here.
        
        // Save over the file
        currFile = fc.getSelectedFile();
        saveObjects();
    }//GEN-LAST:event_jSaveAsMenuItemActionPerformed

    private void jSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveMenuItemActionPerformed
        // Save over the file 
        saveObjects();
    }//GEN-LAST:event_jSaveMenuItemActionPerformed

    private void jLoadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoadMenuItemActionPerformed
        final JFileChooser fc;
                
        // Create a file dialog, using current directory if we have one 
        if ((currFile != null) && (currFile.getParent() != null))
            fc = new JFileChooser(currFile.getParent());
        else
            fc = new JFileChooser(".\\");
        
        // Display file dialog, wait for user response
        int returnVal = fc.showOpenDialog(ObjectViewer.this);

        // If the user cancels, abort function
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        
        // Load the beliefs into our viewer 
        currFile = fc.getSelectedFile();
        loadObjects();
        
        // Update our viewer 
        updateObjects();
        updateObjectPanel(null);
    }//GEN-LAST:event_jLoadMenuItemActionPerformed

    private void jFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileMenuActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jFileMenuActionPerformed
    
    /**
     * Launches a single instance of the ObjectViewer class.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ObjectViewer().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jBeliefPanel;
    private javax.swing.JScrollPane jBeliefScrollPane;
    private javax.swing.JTree jBeliefTree;
    private javax.swing.JScrollPane jClassScrollPane;
    private javax.swing.JTextPane jClassTextPane;
    private javax.swing.JMenuItem jCloseMenuItem;
    private javax.swing.JMenu jFileMenu;
    private javax.swing.JSeparator jFileSeparator;
    private javax.swing.JMenuItem jLoadMenuItem;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JScrollPane jNameScrollPane;
    private javax.swing.JTextPane jNameTextPane;
    private javax.swing.JMenuItem jSaveAsMenuItem;
    private javax.swing.JMenuItem jSaveMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane;
    // End of variables declaration//GEN-END:variables
    
}
