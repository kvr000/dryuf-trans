<?php

namespace net\dryuf\trans\php\test;


/**
 * Class to test parameterized types translation.
 * 
 * @author
 * 	Zbyněk Vyškovský
 * @since
 * 	2015-06-05
 */
class PhpTransTestedTypeParams extends \net\dryuf\util\HashMap
{
	/**
	*/
	function			__construct()
	{
		parent::__construct();
	}

	/**
	@\net\dryuf\core\Type(type = 'long')
	*/
	const				serialVersionUID = 0;

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	public function			put($key, $value)
	{
		if (!is_null($this->get($key)))
			$value = ($this->get($key).$value);
		parent::put($key, $value);
		return $value;
	}

	/**
	@\net\dryuf\core\Type(type = 'int')
	*/
	public function			testFields()
	{
		return \net\dryuf\core\Dryuf::hashCodeObject($this->currentT)+strlen($this->currentU)+\net\dryuf\core\Dryuf::hashCodeObject($this->currentT)+strlen($this->currentU);
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Object')
	*/
	protected			$currentT;

	/**
	@\net\dryuf\core\Type(type = 'java\lang\String')
	*/
	protected			$currentU;
};


?>
