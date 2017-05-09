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
package AirSim.Configs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class BuildXml {

    static public void makeBuildXml(String loc, String builtJarFullPath, String configName, String configParamString) {
	// NOTE: Everything else but three lines is just strings
	//
	// 1) builtJarFullPath - the path and filename for the built
	// jar file of all the code.  Need to check with Ian if the
	// filename itself is required to be accast.jar
	//
	// In ian's version builtJarFullPath was "/projects/ACCAST/dist/accast.jar"
	// In mine it was "G:/ACCAST/sanjaya/dist/accast.jar"
	//
	// 2) configName - the name of the config which also is the
	// name of the dir the config is in.  I tend to use
	// descriptive names, starting with some base and then
	// whatever params, I.e. gascola_uav02, gascola_uav03,
	// gascola_uav04, etc.  If I had another param, say number of
	// emitters, the the name might be gascola_uav03_emitter01,
	// etc.  For accast I'd like to stick with
	// accast_versionN_FalconViewOn_SimUserOff for now.
	//
	// 3) A string with all of the command line params to use when
	// running ACCASTCreate, in order to create the desired
	// config.  I.e.;
	//
	// --rssiuav 2 --auav 0 --eoiruav 0 --uav 0 --ugs 0 --emitters 0 --civilians 0 --operators 1 --ovcm --deconflict --configuration configureFor1by1Gascola
	//
	// for --configuration we only have configureFor5by5AccastDemo
	// for now.

	String locs = loc+File.separator;
        String filename = locs+"build.xml";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("<!-- Sanjaya build file.  Make sure you correctly specify the directory properties below. -->\n");
            out.writeBytes("<project name=\"sanjaya\" default=\"test\" basedir=\".\">\n");
            out.writeBytes("\n");
            out.writeBytes("  <property name=\"switchclass\" value=\"Machinetta.Communication.UDPSwitch\"/>\n");
            out.writeBytes("  <property name=\"sanjayaclass\" value=\"AirSim.Environment.GUI.MainFrame\"/>\n");
            out.writeBytes("  <property name=\"proxyclass\" value=\"Machinetta.Proxy\"/>\n");
            out.writeBytes("  <property name=\"configclass\" value=\"AirSim.Configs.L3Create\"/>\n");
            out.writeBytes("\n");
            out.writeBytes("  <property name=\"serverclass\" value=\"com.lmco.atl.accast.comms.Server\"/>\n");
            out.writeBytes("  <property name=\"clientclass\" value=\"com.lmco.atl.accast.comms.Client\"/>\n");
            out.writeBytes("\n");
            out.writeBytes("  <property name=\"build\" value=\"build\"/>\n");
            out.writeBytes("  <property name=\"configs\" value=\"configs\"/>\n");
            out.writeBytes("  <property name=\"dist\" value=\"dist\"/>\n");
            out.writeBytes("  <property name=\"lib\" value=\"lib\"/>\n");
            out.writeBytes("\n");
            out.writeBytes("  <property name=\"src1\" value=\"AirSim-src\"/>\n");
            out.writeBytes("  <property name=\"src2\" value=\"Gui-src\"/>\n");
            out.writeBytes("  <property name=\"src3\" value=\"Machinetta-src\"/>\n");
            out.writeBytes("  <property name=\"src4\" value=\"Util-src\"/>\n");
            out.writeBytes("\n");

	    // NOTE: builtJarFullPath

            out.writeBytes("  <property name=\"accast.jar\" value=\""+
			   builtJarFullPath
			   +"\"/>\n");
            out.writeBytes("\n");

	    // NOTE: configName

            out.writeBytes("  <property name=\"config.name\" value=\""+
			   configName
			   +"\"/>\n");

	    // NOTE: configParamString

            out.writeBytes("  <property name=\"config.params\" value=\""
			   +configParamString
			   +"\"/>\n");

            out.writeBytes("\n");
            out.writeBytes("  <path id=\"build.class.path\">\n");
            out.writeBytes("    <fileset dir=\"${lib}\">\n");
            out.writeBytes("      <include name=\"**/*.jar\"/>\n");
            out.writeBytes("    </fileset>\n");
            out.writeBytes("  </path>\n");
            out.writeBytes("\n");
            out.writeBytes("  <path id=\"runtime.class.path\">\n");
            out.writeBytes("    <fileset dir=\"${lib}\">\n");
            out.writeBytes("      <include name=\"**/*.jar\"/>\n");
            out.writeBytes("    </fileset>\n");
            out.writeBytes("    <pathelement path=\"${accast.jar}\"/>\n");
            out.writeBytes("    <pathelement path=\"${build}\"/>\n");
            out.writeBytes("    <pathelement path=\".\"/>\n");
            out.writeBytes("  </path>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"test\">\n");
            out.writeBytes("    <echo message=\"Hello world!\"/>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"clean\">\n");
            out.writeBytes("    <delete dir=\"${build}\"/>\n");
            out.writeBytes("    <delete dir=\"${dist}\"/>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"compile\">\n");
            out.writeBytes("    <mkdir dir=\"${build}\"/>\n");
            out.writeBytes("    <javac classpathref=\"build.class.path\" debug=\"on\" destdir=\"${build}\">\n");
            out.writeBytes("      <src path=\"${src1}\"/>\n");
            out.writeBytes("      <src path=\"${src2}\"/>\n");
            out.writeBytes("      <src path=\"${src3}\"/>\n");
            out.writeBytes("      <src path=\"${src4}\"/>\n");
            out.writeBytes("    </javac>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"jar\" depends=\"compile\">\n");
            out.writeBytes("    <mkdir dir=\"${dist}\"/>\n");
            out.writeBytes("    <jar jarfile=\"${dist}/sanjaya.jar\" basedir=\"${build}\"/>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"server\">\n");
            out.writeBytes("    <echo message=\"Starting ACCAST server...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${serverclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-Djava.library.path=${lib}\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Dlog4j.configuration=log4j.xml\"/>\n");
            out.writeBytes("      <arg value=\"8888\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"client\">\n");
            out.writeBytes("    <echo message=\"Starting ACCAST client...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${clientclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-Djava.library.path=${lib}\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Dlog4j.configuration=log4j.xml\"/>\n");
            out.writeBytes("      <arg value=\"localhost\"/>\n");
            out.writeBytes("      <arg value=\"8888\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"config\">\n");
            out.writeBytes("    <echo message=\"Setting up configuration for ${config.name}...\"/>\n");
            out.writeBytes("    <mkdir dir=\"${configs}/${config.name}\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${configclass}\" fork=\"true\">\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}\"/>\n");
            out.writeBytes("      <arg line=\"${config.params}\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"switch\">\n");
            out.writeBytes("    <echo message=\"Starting Switch...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${switchclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"sanjaya\">\n");
            out.writeBytes("    <echo message=\"Starting Sanjaya...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${sanjayaclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/Env.txt\"/>\n");
            out.writeBytes("      <arg value=\"--showgui\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"proxy0\">\n");
            out.writeBytes("    <echo message=\"Starting Proxy 0...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${proxyclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/RSSI-UAV0.cfg\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"proxy1\">\n");
            out.writeBytes("    <echo message=\"Starting Proxy 1...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${proxyclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/RSSI-UAV1.cfg\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"operator\">\n");
            out.writeBytes("    <echo message=\"Starting Operator...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${proxyclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/Operator0.cfg\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"falconview\">\n");
            out.writeBytes("    <echo message=\"Starting FalconView...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${proxyclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/FalconView0.cfg\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("  <target name=\"simuser\">\n");
            out.writeBytes("    <echo message=\"Starting SimUser...\"/>\n");
            out.writeBytes("    <java classpathref=\"runtime.class.path\" classname=\"${proxyclass}\" fork=\"true\">\n");
            out.writeBytes("      <jvmarg value=\"-server\"/>\n");
            out.writeBytes("      <jvmarg value=\"-Xmx400m\"/>\n");
            out.writeBytes("      <arg value=\"${configs}/${config.name}/SimUser0.cfg\"/>\n");
            out.writeBytes("    </java>\n");
            out.writeBytes("  </target>\n");
            out.writeBytes("\n");
            out.writeBytes("</project>\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
}
