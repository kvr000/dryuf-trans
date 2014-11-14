<?php

namespace org\druf\trans\php\test;


/**
 * Comments to the class, more comments on next line including some multibyte unicode characters:
 * Žluťoučký kůň úpěl ďábelské ódy.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2013-03-12
 */
class PhpTransTestedTyped extends \org\druf\core\Object
{
	/**
	 * Constructor comments.
	 */
	/**
	*/
	function			__construct()
	{
		$this->listField = new \org\druf\util\LinkedList();
		$this->mapField = new \org\druf\util\php\NativeHashMap();

		parent::__construct();
		$this->listField->add("hello");
		$this->mapField->put(0, "world");
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			constructorTest()
	{
		return strlen((new \org\druf\util\LinkedList())->get(0));
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			fieldTest()
	{
		$s = $this->listField->size();
		$l = strlen($this->listField->get(0));
		$v = strlen($this->mapField->get(0));
		return $s+$l+$v;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			paramTest($listParam, $mapParam)
	{
		$s = $listParam->size();
		$l = strlen($listParam->get(0));
		$v = strlen($mapParam->get(0));
		return $s+$l+$v;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			localTest()
	{
		$listVar = new \org\druf\util\LinkedList();
		$this->listField->add("hello");
		$mapVar = new \org\druf\util\php\NativeHashMap();
		$this->mapField->put(0, "world");
		$s = $listVar->size();
		$l = strlen($listVar->get(0));
		$v = strlen($mapVar->get(0));
		return $s+$l+$v;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			methodMatchTest()
	{
		$i = 0;
		$i += \org\druf\core\Druf::createClassArg0('integer');
		$i += strlen(\org\druf\core\Druf::createClassArg0('string'));
		return $i;
	}

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Listable<java\lang\String>')
	*/
	protected			$listField;

	/**
	@\org\druf\core\Type(type = 'org\druf\util\Map<java\lang\Long, java\lang\String>')
	*/
	protected			$mapField;
};


?>
