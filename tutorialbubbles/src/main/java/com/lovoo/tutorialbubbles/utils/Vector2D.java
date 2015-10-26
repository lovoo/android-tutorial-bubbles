package com.lovoo.tutorialbubbles.utils;

import android.graphics.Matrix;

public class Vector2D {
	
	public float x;
	public float y;
	
	public Vector2D () {
		this.x = 0;
		this.y = 0;
	}
	
	public Vector2D ( Vector2D v ) {
		if (v == null) {
			v = new Vector2D();
		}
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vector2D ( float[] pos ) {
		if (pos == null || pos.length != 2) {
			this.x = 0;
			this.y = 0;
		} else {
			this.x = pos[0];
			this.y = pos[1];
		}
	}
	
	public Vector2D ( float x, float y ) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D set ( Vector2D other ) {
		if (other != null) {
			x = other.x;
			y = other.y;
		}
		return this;
	}
	
	public Vector2D set ( float x, float y ) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public float[] getPos () {
		float[] f = new float[2];
		f[0] = x;
		f[1] = y;
		return f;
	}
	
	public float getLength () {
		if (x == 0f || y == 0f || x + y == 0f) {
			return 0f;
		}
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public boolean isNullVector () {
		return (getLength() == 0f);
	}
	
	public void add ( Vector2D v ) {
		if (v != null) {
			x += v.x;
			y += v.y;
		}
	}
	
	public void subtract ( Vector2D v ) {
		if (v != null) {
			x -= v.x;
			y -= v.y;
		}
	}
	
	public void multiple ( Vector2D v ) {
		if (v != null) {
			x *= v.x;
			y *= v.y;
		}
	}
	
	public void multipleWith ( float value ) {
		x *= value;
		y *= value;
	}
	
	public void transform ( Matrix m ) {
		Vector2D.transform(this, this, m);
	}
	
	public void transform ( Vector2D src, Matrix m ) {
		Vector2D.transform(src, this, m);
	}
	
	public static void transform ( Vector2D src, Vector2D dst, Matrix m ) {
		if (src == null || dst == null || m == null) {
			return;
		}
		float[] p = new float[2];
		p[0] = src.x;
		p[1] = src.y;
		m.mapPoints(p);
		dst.set(p[0], p[1]);
	}
	
	public static Vector2D add ( Vector2D lhs, Vector2D rhs ) {
		if (lhs == null || rhs == null) {
			return null;
		}
		return new Vector2D(lhs.x + rhs.x, lhs.y + rhs.y);
	}
	
	public static Vector2D subtract ( Vector2D lhs, Vector2D rhs ) {
		if (lhs == null || rhs == null) {
			return null;
		}
		return new Vector2D(lhs.x - rhs.x, lhs.y - rhs.y);
	}
	
	public static float getDistance ( Vector2D lhs, Vector2D rhs ) {
		if (lhs == null || rhs == null) {
			return -1f;
		}
		Vector2D delta = Vector2D.subtract(lhs, rhs);
		return delta.getLength();
	}
	
	public static float getSignedAngleBetween ( Vector2D a, Vector2D b ) {
		if (a == null || b == null) {
			return 0f;
		}
		Vector2D na = getNormalized(a);
		Vector2D nb = getNormalized(b);
		
		return (float) (Math.atan2(nb.y, nb.x) - Math.atan2(na.y, na.x));
	}
	
	public static boolean isNullVector ( Vector2D v ) {
		return (v == null || v.getLength() == 0f);
	}
	
	public static Vector2D getNormalized ( Vector2D v ) {
		if (v == null) {
			return null;
		}
		float l = v.getLength();
		if (l == 0) {
			return new Vector2D();
		} else {
			return new Vector2D(v.x / l, v.y / l);
		}
		
	}
	
	public static boolean pointInLine ( Vector2D d, Vector2D a, Vector2D b ) {
		if (((d.x - a.x) * (b.y - a.y) - (d.y - a.y) * (b.x - a.x)) > 0) {
//			LogUtils.log("Vector2D", String.format("Schnitt Punkt: %s positiv mit Linie %s - %s", d.toShortString(), a.toShortString(), b.toShortString()));
			return true;
		}
//		LogUtils.log("Vector2D", String.format("Schnitt Punkt: %s negativ mit Linie %s - %s", d.toShortString(), a.toShortString(), b.toShortString()));
		return false;
	}
	
	@Override
	public String toString () {
		return String.format("(%.4f, %.4f)", x, y);
	}
	
	public String toShortString () {
		return String.format("(%.0f, %.0f)", x, y);
	}
	
}
