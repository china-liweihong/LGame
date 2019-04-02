/**
 * Copyright 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email javachenpeng@yahoo.com
 * @version 0.4.2
 */
package loon.action.map;

import loon.LSystem;
import loon.geom.Path;
import loon.geom.RectBox;

public class Hexagon {

	private Path path = null;

	protected int x, y, halfWidth, midHeight, endHeight;

	private int[] center = null;

	private int[][] endpoints = null;

	public Hexagon() {
		this(0, 0, 0, 0, 0);
	}

	public Hexagon(int x, int y, int halfWidth, int midHeight, int endHeight) {
		this.x = x;
		this.y = y;
		this.halfWidth = halfWidth;
		this.midHeight = midHeight;
		this.endHeight = endHeight;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getHalfWidth() {
		return halfWidth;
	}

	public int getWidth() {
		return halfWidth + halfWidth;
	}

	public int getMidHeight() {
		return midHeight;
	}

	public int getEndHeight() {
		return endHeight;
	}

	public int getBaseHeight() {
		return endHeight + midHeight;
	}

	public int getHeight() {
		return endHeight + midHeight + midHeight;
	}

	private RectBox frameRect = null;

	public RectBox getFrameRect() {
		if (frameRect == null) {
			frameRect = new RectBox(x, y, x + halfWidth + halfWidth, y + endHeight + midHeight + endHeight);
		}
		return frameRect;
	}

	public int[] getCenter() {
		if (center == null) {
			center = new int[] { x + halfWidth, y + endHeight + (midHeight >> 1) };
		}
		return center;
	}

	public int[][] getEndpoints() {
		if (endpoints == null) {
			int x1 = x + halfWidth;
			int x2 = x + halfWidth + halfWidth;
			int y1 = y + endHeight;
			int y2 = y + endHeight + midHeight;
			int y3 = y + endHeight + midHeight + endHeight;
			endpoints = new int[][] { new int[] { x1, y }, new int[] { x2, y1 }, new int[] { x2, y2 },
					new int[] { x1, y3 }, new int[] { x, y2 }, new int[] { x, y1 } };
		}
		return endpoints;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = LSystem.unite(result, endHeight);
		result = LSystem.unite(result, halfWidth);
		result = LSystem.unite(result, midHeight);
		result = LSystem.unite(result, x);
		result = LSystem.unite(result, y);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hexagon other = (Hexagon) obj;
		if (endHeight != other.endHeight)
			return false;
		if (halfWidth != other.halfWidth)
			return false;
		if (midHeight != other.midHeight)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public Path getPath() {
		if (path == null) {
			path = new Path(0, 0);
			getPath(path);
		}
		return path;
	}

	public Path getPath(Path path) {
		int[][] endpoints = getEndpoints();
		path.moveTo(endpoints[0][0], endpoints[0][1]);
		path.lineTo(endpoints[1][0], endpoints[1][1]);
		path.lineTo(endpoints[2][0], endpoints[2][1]);
		path.lineTo(endpoints[3][0], endpoints[3][1]);
		path.lineTo(endpoints[4][0], endpoints[4][1]);
		path.lineTo(endpoints[5][0], endpoints[5][1]);
		path.close();
		return path;
	}
}
