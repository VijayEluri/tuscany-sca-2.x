/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.diagram.layout;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EntityBuilder {

    private Document dom;

    //components connected to each other are tracked using following map
    private HashMap<String, ArrayList<String>> connectedEntities = new HashMap<String, ArrayList<String>>();
    private int totalWidth = 0;
    private int totalHeight = 0;

    CompositeEntity composite = null;

    /**
     * Constructor which initiates the DOM document
     * @param aDom DOM document
     */
    public EntityBuilder(Document aDom) {
        dom = aDom;
    }

    public CompositeEntity buildCompositeEntity() {

        //get the root element
        Element docEle = dom.getDocumentElement();

        String compositeName;
        compositeName = docEle.getAttribute("name");
        //System.out.println("compositeName "+compositeName);

        ComponentEntity[] comps = buildComponentEntities(docEle);

        composite = new CompositeEntity(compositeName);

        setParent(comps);

        //System.out.println("ComponentEntity "+comps[0].getLevel());
        int[][] conns = buildConnectionMatrix(comps);

        composite.setComponentList(comps);
        composite.setConnections(conns);

        LayoutBuilder buildLayout = new LayoutBuilder(comps, conns);
        buildLayout.placeEntities();

        //System.out.println("conns "+conns[0][0]);

        buildCompositeService(docEle);
        buildCompositeReference(docEle);
        buildCompositeProperty(docEle);

        addInclusions(docEle);

        composite.setAttributes();

        return composite;
    }

    //	private void assignCoordinates() {
    //
    //		for(Entity ent: elts){
    //			ent.setX(ent.getParent().getX() + ent.getStartPosition());
    //			ent.setY(ent.getParent().getY() + ent.getStartPosition()/2);
    //		}
    //	}

    private void setParent(ComponentEntity[] comps) {

        for (ComponentEntity comp : comps) {
            comp.setParent(composite);
        }
    }

    private void buildCompositeService(Element docEle) {

        NodeList nl = docEle.getElementsByTagName("service");
        //System.err.println("^^^^^^^^^ "+nl.getLength());
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                Element elt = (Element)nl.item(i);

                if (elt.getParentNode().getNodeName().equals("composite")) {
                    String compositeSer = elt.getAttribute("name");
                    composite.addAService(compositeSer);

                    String target = elt.getAttribute("promote");

                    String service, serviceComp;
                    String[] arr1 = extractComp(target);
                    serviceComp = arr1[0];
                    service = arr1[1];

                    if (service == null) {
                        composite.addToPromoteAService(compositeSer, serviceComp);
                    } else {
                        composite.addToPromoteAService(compositeSer, service);
                    }
                }

            }
        }
    }

    private void buildCompositeReference(Element docEle) {

        NodeList nl = docEle.getElementsByTagName("reference");
        //System.out.println("^^^^^^^^^ "+nl.getLength());
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                Element elt = (Element)nl.item(i);

                if (elt.getParentNode().getNodeName().equals("composite")) {
                    String compositeRef = elt.getAttribute("name");
                    composite.addAReference(compositeRef);

                    String targetStr = elt.getAttribute("promote");

                    String[] targets = targetStr.split(" ");

                    for (String target : targets) {

                        String reference, referenceComp;
                        String[] arr1 = extractComp(target);
                        referenceComp = arr1[0];
                        reference = arr1[1];

                        if (reference == null) {
                            composite.addToPromoteAReference(compositeRef, referenceComp);
                        } else {
                            composite.addToPromoteAReference(compositeRef, reference);
                        }
                    }

                }
            }
        }
    }

    private void buildCompositeProperty(Element docEle) {

        NodeList nl = docEle.getElementsByTagName("property");
        //System.out.println("^^^^^^^^^ "+nl.getLength());
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                Element elt = (Element)nl.item(i);

                if (elt.getParentNode().getNodeName().equals("composite")) {
                    String compositeProp = elt.getAttribute("name");
                    composite.addAProperty(compositeProp);
                }
            }
        }
    }

    private void addInclusions(Element docEle) {

        NodeList nl = docEle.getElementsByTagName("include");
        //System.out.println("^^^^^^^^^ "+nl.getLength());
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                Element elt = (Element)nl.item(i);

                if (elt.getParentNode().getNodeName().equals("composite")) {
                    String compToBeIncluded = elt.getAttribute("name");
                    composite.addToIncludedComposites(compToBeIncluded);
                }
            }
        }
    }

    private int[][] buildConnectionMatrix(ComponentEntity[] comps) {

        int[][] connections = new int[comps.length][comps.length];
        connections = initConnections(connections);

        //		//sec. 5.4 in the spec
        //		NodeList nl = docEle.getElementsByTagName("wire");
        //		//System.out.println("^^^^^^^^^ "+nl.getLength());
        //		if(nl != null && nl.getLength() > 0 ) {
        //			
        //			for(int i = 0 ; i < nl.getLength();i++) {
        //				
        //				Element elt = (Element)nl.item(i);
        //				
        //				String source = elt.getAttribute("source");
        //				String target = elt.getAttribute("target");
        //				
        //				String service, serviceComp, reference, referenceComp;
        //				
        //				String[] arr1 = extractComp(target);
        //				serviceComp = arr1[0];
        //				service = arr1[1];
        //				
        //				String[] arr2 = extractComp(source);
        //				referenceComp = arr2[0];
        //				reference = arr2[1];

        //				//System.out.println("^^^^^^^^^ "+source+" ::: "+target);
        //				if(target.contains("/")){
        //					String[] arr = target.split("/");
        //					serviceComp = arr[0];
        //					service = arr[1];
        //				}
        //				else{
        //					serviceComp = target;
        //					service = null;
        //				}
        //				
        //				if(source.contains("/")){
        //					String[] arr = source.split("/");
        //					referenceComp = arr[0];
        //					reference = arr[1];
        //				}
        //				else{
        //					referenceComp = source;
        //					reference = null;
        //				}
        //				//sec. 5.4 in the spec
        //		NodeList nl = docEle.getElementsByTagName("wire");
        //		//System.out.println("^^^^^^^^^ "+nl.getLength());
        //		if(nl != null && nl.getLength() > 0 ) {
        //			
        //			for(int i = 0 ; i < nl.getLength();i++) {
        //				
        //				Element elt = (Element)nl.item(i);
        //				
        //				String source = elt.getAttribute("source");
        //				String target = elt.getAttribute("target");
        //				
        //				String service, serviceComp, reference, referenceComp;
        //				
        //				String[] arr1 = extractComp(target);
        //				serviceComp = arr1[0];
        //				service = arr1[1];
        //				
        //				String[] arr2 = extractComp(source);
        //				referenceComp = arr2[0];
        //				reference = arr2[1];

        for (Entity ent : comps) {
            for (String name : ent.getAdjacentEntities()) {
                ComponentEntity e2 = findEntity(comps, name);
                if (ent != null && e2 != null) {
                    //System.out.println("^^^^^^^^^ "+e2.getName());
                    connections[ent.getId()][e2.getId()] = 1;
                }
            }

        }
        //				ComponentEntity e1 = findEntity(comps, referenceComp);
        //				ComponentEntity e2 = findEntity(comps, serviceComp);
        //				
        //				System.out.println("^^^^^^^^^ "+e1.getName());
        //				if(e1 != null && e2 != null){
        //					System.out.println("^^^^^^^^^ "+e1.getId());
        //					connections[e1.getId()][e2.getId()] = 1;
        //					createConnection(e1, reference, serviceComp, service);
        //				}
        //			}
        //		}
        //		
        return connections;
    }

    private String[] extractComp(String str) {

        String[] arr = new String[2];

        if (str.contains("/")) {
            arr = str.split("/");
        } else {
            arr[0] = str;
            arr[1] = null;
        }
        return arr;
    }

    private int[][] initConnections(int[][] connections) {

        for (int i = 0; i < connections.length; i++) {
            for (int j = 0; j < connections.length; j++) {
                connections[i][j] = 0;
            }
        }
        return connections;
    }

    public ComponentEntity[] buildComponentEntities(Element docEle) {

        ComponentEntity[] elts = null;

        //		//get the root element
        //		Element docEle = dom.getDocumentElement();
        //		compositeName = docEle.getAttribute("name");
        //		System.out.println("compositeName "+compositeName);

        //get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName("component");
        if (nl != null && nl.getLength() > 0) {
            elts = new ComponentEntity[nl.getLength()];

            for (int i = 0; i < nl.getLength(); i++) {
                elts[i] = new ComponentEntity();
                Element nVal = (Element)nl.item(i);
                //System.out.println(nVal.hasAttribute("name"));
                elts[i].setId(i);
                elts[i].setName(nVal.getAttribute("name"));

                setServices(nVal, elts[i]);
                setReferences(nVal, elts[i]);
                setProperties(nVal, elts[i]);

                elts[i].referenceHeight();
                elts[i].serviceHeight();
                elts[i].propertyLength();
            }
        }

        buildWires(docEle, elts);
        //		//sec. 5.4 in the spec
        //		nl = docEle.getElementsByTagName("wire");
        //		System.out.println("^^^^^^^^^ "+nl.getLength());
        //		if(nl != null && nl.getLength() > 0 ) {
        //			for(int i = 0 ; i < nl.getLength();i++) {
        //				Element elt = (Element)nl.item(i);
        //				String source = elt.getAttribute("source");
        //				String target = elt.getAttribute("target");
        //				String service, serviceComp, reference, referenceComp;
        //				
        //				System.out.println("^^^^^^^^^ "+source+" ::: "+target);
        //				if(target.contains("/")){
        //					String[] arr = target.split("/");
        //					serviceComp = arr[0];
        //					service = arr[1];
        //				}
        //				else{
        //					serviceComp = target;
        //					service = null;
        //				}
        //				
        //				if(source.contains("/")){
        //					String[] arr = source.split("/");
        //					referenceComp = arr[0];
        //					reference = arr[1];
        //				}
        //				else{
        //					referenceComp = source;
        //					reference = null;
        //				}
        //				
        //				ComponentEntity e = findEntity(referenceComp);
        //				System.out.println("^^^^^^^^^ "+e.getName());
        //				if(e != null){
        //					createConnection(e, reference, serviceComp, service);
        //				}
        //			}
        //		}
        //
        //		positionEntities(elts);
        //
        //		calculateProperties(elts);
        //	print(elts);

        return elts;

    }

    private void buildWires(Element docEle, ComponentEntity[] elts) {

        //sec. 5.4 in the spec
        NodeList nl = docEle.getElementsByTagName("wire");
        //System.out.println("^^^^^^^^^ "+nl.getLength());
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                Element elt = (Element)nl.item(i);

                String source = elt.getAttribute("source");
                String target = elt.getAttribute("target");

                String service, serviceComp, reference, referenceComp;

                String[] arr1 = extractComp(target);
                serviceComp = arr1[0];
                service = arr1[1];

                String[] arr2 = extractComp(source);
                referenceComp = arr2[0];
                reference = arr2[1];

                //				//System.out.println("^^^^^^^^^ "+source+" ::: "+target);
                //				if(target.contains("/")){
                //					String[] arr = target.split("/");
                //					serviceComp = arr[0];
                //					service = arr[1];
                //				}
                //				else{
                //					serviceComp = target;
                //					service = null;
                //				}
                //				
                //				if(source.contains("/")){
                //					String[] arr = source.split("/");
                //					referenceComp = arr[0];
                //					reference = arr[1];
                //				}
                //				else{
                //					referenceComp = source;
                //					reference = null;
                //				}
                //				
                ComponentEntity e1 = findEntity(elts, referenceComp);
                //ComponentEntity e2 = findEntity(comps, serviceComp);

                //System.out.println("^^^^^^^^^ "+e1.getName());
                if (e1 != null) {
                    //System.out.println("^^^^^^^^^ "+e1.getId());
                    //connections[e1.getId()][e2.getId()] = 1;
                    createConnection(e1, reference, serviceComp, service);
                }
            }
        }

    }

    private ComponentEntity findEntity(ComponentEntity[] elts, String componentName) {

        for (ComponentEntity e : elts) {
            if (e.getName().equals(componentName)) {
                return e;
            }
        }
        return null;
    }

    private void setReferences(Element nVal, ComponentEntity ent) {

        NodeList nl = nVal.getElementsByTagName("reference");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element elt = (Element)nl.item(i);
                String target = elt.getAttribute("target");
                String ref = elt.getAttribute("name");
                if (target.contains("/")) {
                    String[] arr = target.split("/");
                    createConnection(ent, ref, arr[0], arr[1]);
                    //					ent.addToRefToSerMap(ref, arr[1]);
                    //					ent.addAnAdjacentEntity(arr[0]);
                    //					addToConnectedEntities(ent.getComponentName(), arr[0]);
                    //					addToConnectedEntities(arr[0], ent.getComponentName());
                } else if (!target.equals("")) {
                    createConnection(ent, ref, target, null);
                    //					ent.addToRefToSerMap(ref, target);
                    //					ent.addAnAdjacentEntity(target);
                    //					addToConnectedEntities(ent.getComponentName(), target);
                    //					addToConnectedEntities(target, ent.getComponentName());
                }

                ent.addAReference(ref);

            }
        }
    }

    private void createConnection(ComponentEntity ent, String reference, String serviceComp, String service) {

        String referenceComp = ent.getName();

        if (reference != null && service != null) {

            ent.addToRefToSerMap(reference, service);
            ent.addAnAdjacentEntity(serviceComp);
            addToConnectedEntities(referenceComp, serviceComp);
            addToConnectedEntities(serviceComp, referenceComp);
        } else if (reference == null && service != null) {
            ent.addToRefToSerMap(referenceComp, service);
            ent.addAnAdjacentEntity(serviceComp);
            addToConnectedEntities(referenceComp, serviceComp);
            addToConnectedEntities(serviceComp, referenceComp);
        } else if (reference != null && service == null) {
            ent.addToRefToSerMap(reference, serviceComp);
            ent.addAnAdjacentEntity(serviceComp);
            addToConnectedEntities(referenceComp, serviceComp);
            addToConnectedEntities(serviceComp, referenceComp);
        } else {
            ent.addToRefToSerMap(referenceComp, serviceComp);
            ent.addAnAdjacentEntity(serviceComp);
            addToConnectedEntities(referenceComp, serviceComp);
            addToConnectedEntities(serviceComp, referenceComp);
        }
    }

    //	private void calculateProperties(ComponentEntity[] elts) {
    //		int level=0, lane=0;
    //
    //		for(ComponentEntity ent: elts){
    //			level = max(level, ent.getLevel());
    //			lane = max(lane, ent.getLane());
    //
    //		}
    //		totalHeight += spaceY*(level+1) + initPoint;
    //		totalWidth += spaceX*(lane+1) + initPoint;
    //		
    //		System.err.println(totalHeight + " :: "+totalWidth);
    //	}

    //	private int max(int a, int b){
    //		if(a>=b)
    //			return a;
    //		return b;
    //	}

    @SuppressWarnings("unused")
    private void print(ComponentEntity[] elts) {

        for (ComponentEntity ent : elts) {
            System.out.println(ent.getName() + " : "
                + ent.getLevel()
                + " : "
                + ent.getLane()
                + " : "
                + ent.getX()
                + " : "
                + ent.getY());
        }
    }

    //	private void positionEntities(ComponentEntity[] ents){
    //		
    //		for(ComponentEntity ent: ents){
    //			if(ent.getAdjacentEntities().size() != 0 || ents.length==1){
    //				setPosition(ent, initPoint, initPoint, 0, 0);
    //				levelCount.add(0, 1);
    //				startEnt = ent;
    //				System.err.println(ent.getName());
    //				break;
    //			}
    //		}
    //		
    //
    //		if(startEnt != null)
    //			assignPositions(ents, startEnt);
    //
    //	}
    //
    //	private void assignPositions(ComponentEntity[] ents, ComponentEntity ent){
    //		int i=0;
    //		if(ent.getAdjacentEntities().size()>0){
    //			
    //			System.out.println(ent.getName());
    //			for(String name: ent.getAdjacentEntities()){
    //				//System.out.println("eee "+name);
    //				for(ComponentEntity aEnt: ents){
    //					i++;
    //					if(name.equalsIgnoreCase(aEnt.getName())){
    //						int lane = ent.getLane()+1;
    //						if(levelCount.size()<= lane){
    //							levelCount.add(lane, 1);
    //							setPosition(aEnt, ent.getX()+spaceX, ent.getY(), 0, lane);
    //						}
    //						else{
    //							int level = levelCount.get(lane);
    //							levelCount.add(lane, level+1);
    //							setPosition(aEnt, ent.getX()+spaceX, ent.getY()+spaceY*level, level, lane);
    //						}
    //						if(i<ents.length)
    //							assignPositions(ents, aEnt);
    ////						else
    ////							System.out.println(i+ " <<<<< "+ents.length);
    //						break;
    //					}
    //
    //				}
    //			}
    //		}
    //
    //
    //		else{
    //			ArrayList<String> conns = connectedEntities.get(ent.getName());
    //			System.err.println(conns.size());
    //			if(conns.size()>0){
    //
    //				for(String conn: conns){
    //					System.err.println("conn "+conn +" : "+ent.getName());
    //					for(ComponentEntity e: ents){
    //						if(e.getLane() == -1 && e.getName().equals(conn)){
    //
    //							int lane = ent.getLane()-1;
    //							System.err.println(lane);
    //							int level = levelCount.get(lane);
    //							levelCount.add(lane, level+1);
    //							setPosition(e, ent.getX()-spaceX, ent.getY()+spaceY*level, level, lane);
    //
    //							break;
    //						}
    //					}
    //				}
    //			}
    //		}
    //	}
    //
    //	private void setPosition(ComponentEntity ent, int x, int y, int level, int lane){
    //		ent.setX(x);
    //		ent.setY(y);
    //		ent.setLevel(level);
    //		ent.setLane(lane);
    //	}
    //
    //
    //	private String[] splitValues(String str){
    //		return str.split("/");
    //	}

    private void addToConnectedEntities(String ent1, String ent2) {
        //System.err.println(ent1+" : "+ent2);
        ArrayList<String> list;
        if (connectedEntities.containsKey(ent1)) {
            list = connectedEntities.get(ent1);

        } else {
            list = new ArrayList<String>();

        }
        list.add(ent2);
        connectedEntities.put(ent1, list);
    }

    private void setServices(Element nVal, ComponentEntity ent) {

        NodeList nl = nVal.getElementsByTagName("service");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element elt = (Element)nl.item(i);
                ent.addAService(elt.getAttribute("name"));
            }
        } else {

            NodeList nl1 = nVal.getElementsByTagName("implementation.java");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    Element elt = (Element)nl1.item(i);
                    //System.out.println(elt.getAttribute("class"));
                    String serName = extractServiceName(elt.getAttribute("class"));
                    ent.addAService(serName);
                }
            }

        }

    }

    /**
     * 
     * This will extract the service name part from the class attribute of 
     * implementation.java element. 
     * eg: if class = "NirmalServiceImpl", returning service name would be "NirmalService"
     */
    private String extractServiceName(String classAttr) {
        if (classAttr != null) {
            String[] x = classAttr.split("\\.");
            String name = x[x.length - 1];
            if (name.endsWith("Impl")) {
                return name.substring(0, name.length() - 4);
            } else {
                return name;
            }
        }
        return "";
    }

    private void setProperties(Element nVal, ComponentEntity ent) {

        NodeList nl = nVal.getElementsByTagName("property");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element elt = (Element)nl.item(i);
                ent.addAProperty(elt.getAttribute("name"));
            }
        }
    }

    //	public void setCompositeName(String compositeName) {
    //		this.compositeName = compositeName;
    //	}
    //
    //	public String getCompositeName() {
    //		return compositeName;
    //	}

    public int getTotalWidth() {
        return totalWidth;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

}