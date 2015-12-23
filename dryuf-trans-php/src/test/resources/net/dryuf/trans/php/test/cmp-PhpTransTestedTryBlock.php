<?php

namespace net\dryuf\trans\php\test;


/**
 * Class to test try blocks
 * 
 * @author
 * 	Zbyněk Vyškovský
 */
class PhpTransTestedTryBlock extends \net\dryuf\core\Object
{
	/**
	*/
	function			__construct()
	{
		parent::__construct();
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			basicTryCatch()
	{
		try {
			++$this->i;
		}
		catch (\net\dryuf\core\Exception $ex) {
			throw new \net\dryuf\core\RuntimeException($ex);
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			tryFinally()
	{
		try {
			++$this->i;
		}
		finally {
			--$this->i;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			tryResourcesFull()
	{
		try {
			$stream = new \java\io\FileInputStream("a.txt");
			++$this->i;
		}
		catch (\net\dryuf\core\Exception $ex) {
			throw new \net\dryuf\core\RuntimeException($ex);
		}
		finally {
			--$this->i;
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'void')
	*/
	public function			tryResourcesEmpty()
	{
		try {
			$stream = new \java\io\FileInputStream("a.txt");
			++$this->i;
		}
		finally {
		}
	}

	/**
	@\net\dryuf\core\Type(type = 'java\lang\Integer')
	*/
	protected			$i = 0;
};


?>
