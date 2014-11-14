<?php

namespace org\druf\trans\php\test;


/**
 * Class to test parameterized types translation.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-05
 */
class PhpTransTestedTypeParams extends \org\druf\util\HashMap
{
	/**
	*/
	function			__construct()
	{
		parent::__construct();
	}

	/**
	@\org\druf\core\Type(type = 'long')
	*/
	const				serialVersionUID = 0;

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	public function			put($key, $value)
	{
		if (!is_null($this->get($key)))
			$value = ($this->get($key).$value);
		parent::put($key, $value);
		return $value;
	}

	/**
	@\org\druf\core\Type(type = 'int')
	*/
	public function			testFields()
	{
		return \org\druf\core\Druf::hashCodeObject($this->currentT)+strlen($this->currentU)+\org\druf\core\Druf::hashCodeObject($this->currentT)+strlen($this->currentU);
	}

	/**
	@\org\druf\core\Type(type = 'java\lang\Object')
	*/
	protected			$currentT;

	/**
	@\org\druf\core\Type(type = 'java\lang\String')
	*/
	protected			$currentU;
};


?>
