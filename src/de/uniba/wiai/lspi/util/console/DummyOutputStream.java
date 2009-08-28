/***************************************************************************
 *                                                                         *
 *                           DummyOutputStream.java                        *
 *                            -------------------                          *
 *   date                 : 8. September 2004, 19:01                       *
 *   copyright            : (C) 2004 Distributed and Mobile Systems Group  *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/

package de.uniba.wiai.lspi.util.console;

import java.io.IOException;

/**
 * Dummy output stream that writes nothing. Can be used to substitute
 * {@link System#out} with, so that no calls to e.g.
 * <code>System.out.println()</code> are displayed in the console window. The
 * calls result in nothing being printed.
 * 
 * @author sven
 * @version 1.0.5
 */
public class DummyOutputStream extends java.io.OutputStream {

	/** Creates a new instance of DummyOutputStream */
	public DummyOutputStream() {
		/* nothing to do here */
	}

	/**
	 * Overwritten from {@link java.io.OutputStream}. This method does nothing.
	 * The byte that should be written is thrown away.
	 * 
	 * @param b
	 *            This byte is thrown away. Nothing is really written.
	 * @throws IOException
	 *             Does not occur as nothing is really written.
	 * 
	 */
	public void write(int b) throws IOException {
		/* nothing to do here */
	}

}
