
package org.heinz.framework.utils;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JFrame;

public abstract class FileDragAndDrop implements DropTargetListener {

	public abstract void filesDropped(List files);

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public FileDragAndDrop(Component component) {
		new DropTarget(component, this);
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	@SuppressWarnings({"UseSpecificCatch", "CallToPrintStackTrace"})
	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();

			for(DataFlavor flavor : flavors) {
				if(flavor.isFlavorJavaFileListType()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List files = (List) tr.getTransferData(flavor);
					dtde.dropComplete(true);
					filesDropped(files);
					return;
				} else if(flavor.isRepresentationClassInputStream()) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					ByteArrayOutputStream bos;
					try (InputStream is = (InputStream) tr.getTransferData(flavor)) {
						bos = new ByteArrayOutputStream();
						int ch;
						while((ch = is.read()) >= 0) {
							// C-style termination
							if(ch > 0) {
								bos.write(ch);
							}
						}
					}
					bos.close();
					String files = new String(bos.toByteArray());
					dtde.dropComplete(true);
					List fileList = parseUris(files);
					filesDropped(fileList);
					return;
				}
			}
			dtde.rejectDrop();
		} catch(Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

	@SuppressWarnings("CallToPrintStackTrace")
	private List parseUris(String uris) {
		List files = new ArrayList();
		StringTokenizer st = new StringTokenizer(uris, "\n\r ");
		while(st.hasMoreTokens()) {
			String tok = st.nextToken();
			try {
				URI uri = new URI(tok);
				File file = new File(uri);
				files.add(file);
			} catch(URISyntaxException e) {
				// impossible
				e.printStackTrace();
			}
		}
		return files;
	}

	public static void main(String args[]) {
		JFrame f = new JFrame("Test");
		new FileDragAndDrop(f.getContentPane()) {

			@Override
			public void filesDropped(List files) {
				System.out.println("Dropped:");
				for(Iterator it = files.iterator(); it.hasNext();) {
					File file = (File) it.next();
					System.out.println("  " + file);
				}
			}

		};
		f.setSize(300, 400);
		f.setVisible(true);
	}

}
