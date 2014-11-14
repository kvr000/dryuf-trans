/*
 * druf library
 *
 * druf multiplatform development toolkit
 *
 * ----------------------------------------------------------------------------------
 *
 * Copyright (C) 2013-2015 Zbyněk Vyškovský
 *
 * ----------------------------------------------------------------------------------
 *
 * LICENSE:
 *
 * This file is part of druf
 *
 * druf is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * druf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with druf; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * @author	2013-2015 Zbyněk Vyškovský
 * @link	mailto:kvr@matfyz.cz
 * @link	http://kvr.matfyz.cz/software/java/druf/trans/
 * @license	http://www.gnu.org/licenses/lgpl.txt GNU Lesser General Public License v3
 */
package org.druf.trans;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class BoundTypeResolver extends java.lang.Object
{
	public static Class<?>		getRawClass(Type type)
	{
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType) {
			return getRawClass(((ParameterizedType)type).getRawType());
		}
		else {
			throw new RuntimeException("Invalid class passed to getRawClass: "+type.getClass()+", value: "+type.toString());
		}
	}

	public static BoundType		translatePrimitiveToWrap(BoundType bound)
	{
		Class<?> wrap;
		if ((wrap = ReflectionUtil.translatePrimitiveToWrap(bound.getRawClass())) != null)
			return BoundType.createRaw(wrap);
		return null;
	}

	public static BoundType		translatePrimitiveToWrapOrOriginal(BoundType bound)
	{
		Class<?> wrap;
		if ((wrap = ReflectionUtil.translatePrimitiveToWrap(bound.getRawClass())) != null)
			return BoundType.createRaw(wrap);
		return bound;
	}

	public static BoundType		resolveClassBasicBounds(Class<?> clazz)
	{
		LinkedList<BoundType> bounds = new LinkedList<>();
		if (clazz == Enum.class) {
			bounds.add(BoundType.createRaw(getRawClass(clazz.getTypeParameters()[0].getBounds()[0])));
		}
		else if (clazz.getTypeParameters() != null) {
			for (TypeVariable<?> typeParameter: clazz.getTypeParameters()) {
				bounds.add(resolveClassBasicBounds(typeParameter.getBounds() != null && typeParameter.getBounds().length > 0 ? getRawClass(typeParameter.getBounds()[0]) : Object.class));
			}
		}
		return BoundType.createTyped(clazz, bounds.toArray(new BoundType[bounds.size()]));
	}

	public static BoundType		resolveTypeBasicBounds(Type type)
	{
		if (type instanceof Class) {
			return resolveClassBasicBounds((Class<?>)type);
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType)type;
			LinkedList<BoundType> bounds = new LinkedList<>();
			if (ptype.getActualTypeArguments() != null) {
				for (Type typeParameter: ptype.getActualTypeArguments()) {
					bounds.add(resolveTypeBasicBounds(typeParameter));
				}
			}
			return BoundType.createTyped(getRawClass(type), bounds.toArray(new BoundType[bounds.size()]));
		}
		else {
			throw new RuntimeException("Invalid class passed to getRawClass: "+type.getClass()+", value: "+type.toString());
		}
	}

	public static BoundType		guessReturnTypeFromArguments(Method method, BoundType ownerType, Map<String, BoundType> variableMap, List<BoundType> arguments)
	{
		BoundType result;
		if ((result = guessReturnTypeFromArgumentsInternal(method, ownerType, variableMap, arguments)) == null)
			throw new RuntimeException("Failed to find definition of method "+method+" in class "+ownerType.getRawClass().getName());
		return result;
	}

	public static BoundType		replaceGenericTypes(Class<?> declaringClass, Type defined, BoundType ownerType, Map<String, BoundType> boundsMap)
	{
		BoundType result;
		if ((result = replaceGenericTypesInternal(declaringClass, defined, ownerType, boundsMap)) == null)
			throw new RuntimeException("Failed to find definition of type "+defined+" in class "+declaringClass.getName());
		return result;
	}

	public static BoundType		resolveInheritedBounds(Type type, BoundType subBound)
	{
		try {
			if (type instanceof Class<?>)
				return BoundType.createRaw((Class<?>) type);
			ParameterizedType ptype = (ParameterizedType) type;
			LinkedHashMap<String, BoundType> boundsMap = new LinkedHashMap<>();
			enrichBoundsMap(boundsMap, subBound);
			return resolveInheritedBoundsInternal(ptype, subBound, boundsMap);
		}
		catch (Exception ex) {
			throw new RuntimeException("Error resolving inherited bounds for "+type+" from "+subBound.getRawClass(), ex);
		}
	}


	public static BoundType		castBound(Class<?> targetClass, BoundType subBound)
	{
		return castBoundInternal(targetClass, subBound);
	}

	/**
	 * Computes the bound type from native Type and the map of type variable to real BoundType.
	 *
	 * @param defined
	 * 	the defined type
	 * @param boundsMap
	 * 	map of variables of name to BoundType
	 *
	 * @return
	 * 	bound type computed from native information
	 */
	public static BoundType		createBoundTypeFromNative(Type defined, Map<String, BoundType> boundsMap)
	{
		if (defined instanceof Class) {
			return BoundType.createRaw((Class<?>) defined);
		}
		else if (defined instanceof TypeVariable) {
			BoundType result;
			if ((result = boundsMap.get(defined.toString())) != null)
				return result;
			throw new RuntimeException("Cannot find mapping for "+defined);
		}
		else if (defined instanceof ParameterizedType) {
			ParameterizedType pdefined = (ParameterizedType)defined;
			LinkedList<BoundType> bounds = new LinkedList<>();
			for (Type ptype : pdefined.getActualTypeArguments()) {
				bounds.add(createBoundTypeFromNative(ptype, boundsMap));
			}
			return BoundType.createTyped(getRawClass(pdefined.getRawType()), bounds.toArray(new BoundType[bounds.size()]));
		}
		else if (defined instanceof WildcardType) {
			WildcardType wdefined = (WildcardType)defined;
			return createBoundTypeFromNative(wdefined.getUpperBounds()[0], boundsMap);
		}
		else if (defined instanceof GenericArrayType) {
			GenericArrayType gaDefined = (GenericArrayType) defined;
			return BoundType.createArray(createBoundTypeFromNative(gaDefined.getGenericComponentType(), boundsMap));
		}
		else {
			throw new RuntimeException("Unexpected Type "+defined.getClass().getName()+": "+defined);
		}
	}

	public static BoundType		convertLambdaToClass(Class<?> clazz, BoundType lambdaBounds)
	{
		Method method = null;
		for (Method itm: clazz.getMethods()) {
			if (itm.isDefault())
				continue;
			try {
				Object.class.getMethod(itm.getName(), itm.getParameterTypes());
				continue;
			}
			catch (NoSuchMethodException ex) {
			}
			if (method != null)
				return null;
			method = itm;
		}
		if (lambdaBounds.getTypeArguments().length-1 != method.getParameterTypes().length)
			return null;

		List<BoundType> bounds = new LinkedList<>();
		loopclass: for (TypeVariable<?> typeVar: clazz.getTypeParameters()) {
			if (typeVar.equals(method.getGenericReturnType())) {
				bounds.add(lambdaBounds.getTypeArguments()[lambdaBounds.getTypeArguments().length-1]);
				continue loopclass;
			}
			else {
				for (int i = 0; i < method.getGenericParameterTypes().length; ++i) {
					if (method.getGenericParameterTypes()[i].equals(typeVar)) {
						bounds.add(lambdaBounds.getTypeArguments()[i]);
						continue loopclass;
					}
				}
				return null;
			}
		}
		return BoundType.createTyped(clazz, bounds.toArray(new BoundType[bounds.size()]));
	}

	protected static BoundType	guessReturnTypeFromArgumentsInternal(Method method, BoundType ownerType, Map<String, BoundType> typeVarsMap, List<BoundType> arguments)
	{
		if (ownerType == null)
			throw new IllegalArgumentException("ownerType cannot be null");
		Class<?> rawClass = ownerType.getRawClass();
		if (rawClass == method.getDeclaringClass()) {
			enrichBoundsMap(typeVarsMap, ownerType);
			String pending = "";
			for (int tries1 = 0; tries1 < method.getTypeParameters().length+1 && pending != null; ++tries1) {
				// first try resolving as much as possible using fully typed types
				for (int tries2 = 0; tries2 < method.getTypeParameters().length+1 && pending != null; ++tries2) {
					pending = null;
					for (TypeVariable<?> typeVar: method.getTypeParameters()) {
						if (typeVarsMap.containsKey(typeVar.getName()))
							continue;
						BoundType resolved = guessTypeVariableBoundFromArguments(typeVar, method.getGenericParameterTypes(), arguments.toArray(new BoundType[arguments.size()]), typeVarsMap);
						if (resolved != null) {
							typeVarsMap.put(typeVar.getName(), resolved);
						}
						else {
							pending = typeVar.getName();
						}
					}
				}
				// try resolving the remains with lambda
				for (int tries2 = 0; tries2 < method.getTypeParameters().length+1 && pending != null; ++tries2) {
					pending = null;
					for (TypeVariable<?> typeVar: method.getTypeParameters()) {
						if (typeVarsMap.containsKey(typeVar.getName()))
							continue;
						BoundType resolved = guessTypeVariableBoundFromLambdaArguments(typeVar, method.getGenericParameterTypes(), arguments.toArray(new BoundType[arguments.size()]), typeVarsMap);
						if (resolved != null) {
							typeVarsMap.put(typeVar.getName(), resolved);
						}
						else {
							pending = typeVar.getName();
						}
					}
				}
			}
			if (pending != null)
				throw new RuntimeException("Not able to resolve type variable: "+pending+", method="+method);
			return createBoundTypeFromNative(method.getGenericReturnType(), typeVarsMap);
		}
		else {
			BoundType result;
			if (rawClass.getSuperclass() != null && (result = guessReturnTypeFromArgumentsInternal(method, resolveInheritedBounds(rawClass.getGenericSuperclass(), ownerType), typeVarsMap, arguments)) != null)
				return result;
			for (Type iface: rawClass.getGenericInterfaces()) {
				if ((result = guessReturnTypeFromArgumentsInternal(method, resolveInheritedBounds(iface, ownerType), typeVarsMap, arguments)) != null)
					return result;
			}
			return null;
		}
	}

	protected static BoundType	guessTypeVariableBoundFromArguments(TypeVariable<?> typeVar, Type[] parameters, BoundType[] callBounds, Map<String, BoundType> variableMap)
	{
		for (int i = 0; i < parameters.length; ++i) {
			Type parameter = parameters[i];
			if (parameter instanceof WildcardType) {
				WildcardType wtype = (WildcardType)parameter;
				if (!(wtype.getUpperBounds()[0] instanceof Class)) {
					parameter = wtype.getUpperBounds()[0];
				}
				else if (wtype.getLowerBounds().length > 0 && !(wtype.getLowerBounds()[0] instanceof Class)) {
					// this is a question, do we really want to process "super" wildcard,
					// it's better than nothing but the below is actually not correct
					parameter = wtype.getLowerBounds()[0];
				}
			}
			if (parameter instanceof TypeVariable) {
				TypeVariable<?> tv = (TypeVariable<?>)parameter;
				if (tv.getName().equals(typeVar.getName()))
					return translatePrimitiveToWrapOrOriginal(callBounds[i]);
			}
		}
		for (int i = 0; i < parameters.length; ++i) {
			Type parameter = parameters[i];
			if (parameter instanceof WildcardType) {
				WildcardType wtype = (WildcardType)parameter;
				if (!(wtype.getUpperBounds()[0] instanceof Class)) {
					parameter = wtype.getUpperBounds()[0];
				}
				else if (wtype.getLowerBounds().length > 0 && !(wtype.getLowerBounds()[0] instanceof Class)) {
					parameter = wtype.getLowerBounds()[0];
				}
			}
			if (parameter instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType)parameter;
				BoundType castBound = castBound(getRawClass(ptype), callBounds[i]);
				BoundType result;
				if (castBound != null && (result = guessTypeVariableBoundFromArguments(typeVar, ptype.getActualTypeArguments(), castBound.getTypeArguments(), variableMap)) != null) {
					return result;
				}
			}
			else if (parameter instanceof GenericArrayType) {
				GenericArrayType atype = (GenericArrayType)parameter;
				BoundType result;
				if (callBounds[i].isArray() && (result = guessTypeVariableBoundFromArguments(typeVar, new Type[]{ atype.getGenericComponentType() }, new BoundType[]{ callBounds[i].getElementBound() }, variableMap)) != null)
					return result;
			}
		}
		return null;
	}

	protected static BoundType	guessTypeVariableBoundFromLambdaArguments(TypeVariable<?> typeVar, Type[] parameters, BoundType[] callBounds, Map<String, BoundType> variableMap)
	{
		for (int i = 0; i < parameters.length; ++i) {
			if (parameters[i] instanceof ParameterizedType && callBounds[i].getRawClass() == LambdaWrap.class) {
				ParameterizedType ptype = (ParameterizedType)parameters[i];
				BoundType castBound = convertLambdaToClass(getRawClass(ptype), callBounds[i]);
				BoundType result;
				if (castBound != null && (result = guessTypeVariableBoundFromArguments(typeVar, ptype.getActualTypeArguments(), castBound.getTypeArguments(), variableMap)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	protected static <T extends Map<String, BoundType>> T enrichBoundsMap(T boundsMap, BoundType type)
	{
		Class<?> rawClass = type.getRawClass();

		for (int i = 0; i < rawClass.getTypeParameters().length; ++i) {
			TypeVariable<?> typeVariable = rawClass.getTypeParameters()[i];
			if (boundsMap.containsKey(typeVariable.getName()))
				continue;
			boundsMap.put(typeVariable.getName(), i < type.getTypeArguments().length ? type.getTypeArguments()[i] : resolveTypeBasicBounds(typeVariable.getBounds()[0]));
		}
		return boundsMap;
	}

	protected static BoundType	replaceGenericTypesInternal(Class<?> declaringClass, Type defined, BoundType ownerType, Map<String, BoundType> boundsMap)
	{
		Class<?> rawClass = ownerType.getRawClass();
		if (rawClass == declaringClass) {
			enrichBoundsMap(boundsMap, ownerType);
			return createBoundTypeFromNative(defined, boundsMap);
		}
		else {
			BoundType result;
			if (rawClass.getSuperclass() != null && (result = replaceGenericTypesInternal(declaringClass, defined, resolveInheritedBounds(rawClass.getGenericSuperclass(), ownerType), boundsMap)) != null)
				return result;
			for (Type iface: rawClass.getGenericInterfaces()) {
				if ((result = replaceGenericTypesInternal(declaringClass, defined, resolveInheritedBounds(iface, ownerType), boundsMap)) != null)
					return result;
			}
			return null;
		}
	}

	protected static BoundType	castBoundInternal(Class<?> targetClass, BoundType ownerType)
	{
		Class<?> rawClass = ownerType.getRawClass();
		if (rawClass == targetClass) {
			return ownerType;
		}
		else {
			LinkedHashMap<String, BoundType> boundsMap = new LinkedHashMap<>();
			enrichBoundsMap(boundsMap, ownerType);
			BoundType result;
			if (rawClass.getSuperclass() != null && (result = castBoundInternal(targetClass, resolveInheritedBounds(rawClass.getGenericSuperclass(), ownerType))) != null)
				return result;
			for (Type iface: rawClass.getGenericInterfaces()) {
				if ((result = castBoundInternal(targetClass, resolveInheritedBounds(iface, ownerType))) != null)
					return result;
			}
			return null;
		}
	}

	protected static BoundType	resolveInheritedBoundsInternal(Type type, BoundType subBound, Map<String, BoundType> boundsMap)
	{
		if (type instanceof Class<?>) {
			return BoundType.createRaw((Class<?>)type);
		}
		else if (type instanceof TypeVariable) {
			BoundType result;
			if ((result = boundsMap.get(((TypeVariable)type).getName())) == null)
				throw new RuntimeException("Cannot find type variable "+((TypeVariable)type).getName()+" while processing bounds to type "+type.toString());
			return result;
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			LinkedList<BoundType> resultBounds = new LinkedList<>();
			for (Type parameterType : ptype.getActualTypeArguments()) {
				resultBounds.add(resolveInheritedBoundsInternal(parameterType, subBound, boundsMap));
			}
			return BoundType.createTyped((Class<?>) ptype.getRawType(), resultBounds.toArray(new BoundType[resultBounds.size()]));
		}
		else {
			throw new RuntimeException("Unexpected type: "+type.getClass().getName()+", value="+type.toString());
		}
	}
}
