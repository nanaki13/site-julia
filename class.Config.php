<?php 

class Config{
  private $media;
  private $media2;
  private $gallery ;
  private $config ;
  private $page;
  private $parent;
  private $dao;
  public function __construct($page,$subPage){
	    
    if(isset( $subPage)){
      $this->parent = $page;
      $this->page = $subPage;
    }else{
		$this->page = $page;
    }
	   
    $db = new SQLite3(__DIR__."/julia.db",SQLITE3_OPEN_READWRITE);
	    
    $havRes = $this->findPage($db);
	
	
	if(!$havRes){
  		throw new Exception('404');
	}
}
	public static function getDb(){
		return $db = new SQLite3(__DIR__."/julia.db",SQLITE3_OPEN_READWRITE);
	}

	
		
	
	
	function getConfig(){
		return $this->config;
	}
	
	public function getData($name){
        if($name == 'apropos'){
           
            return $this->dao->getAProposText();
        }else if($name == 'administration_theme'){
				echo 'get data';
				return  $this->dao->getAllThemes();
		}
	}
	public function findPage($db){

		try{
			
			$havRes = false;
			$this->page = str_replace("-"," ", sql_format( $this->page));
			$dao = new Dao($db);
			$this->dao = $dao;
			$update_image = getPage() == 'update_image';
			$delete_media = getPage() == 'delete_media';
			if(isLogged()){
                if($update_image||$delete_media){
                if($update_image){
                    $img = new ImageGallery($_POST);
                    $dao->save($img);
                }else{
                    $dao->delete_media($_POST['id']);
                }
		
 				header('Location: /'.$_POST['page_red']);
 				$_GET['page'] = $_POST['page_red'];
 				$this->page = str_replace("-"," ", sql_format( $_GET['page']));
                }elseif(getPage() == 'upload_form'){
                    require 'upload.php';
                }elseif(getSubPage() == 'createOeuvre'){
                    require 'createOeuvre.php';
                    header('Location: /admin/images');
                    $this->parent = $_GET['page'] = 'admin';
                    $this->page = $_GET['subPage'] = 'images';
                    $havRes = true;
                }elseif(getPage() == 'upload_form'){
                    $havRes = true;
                }
			}
			
			$this->config = $dao->getBasePageConfig($this->page,$this->parent);
			if(isLogged()&& $this->config == false){
					$havRes = true;	
					$this->config = [];
					$this->config['haveGallery']=false;
					$this->config['haveMenu']=false;
					$this->config['haveSubMenu']=false;
					$this->config['name']=$this->page;
					$this->config['dao']=$dao;
			}
			
			if($this->config != false){
				
				if($this->page == "login"){
					
					if(!isLogged() && isset($_POST["login"])){
						$loginName = $_POST["login"];
						$pass = $_POST["password"];
						
						$result2 = $db->query("select name, password from user where name = '".strtolower($loginName)."' and password = '".$pass."'" );
						if($res = $result2->fetchArray(SQLITE3_ASSOC)){
							
							$user = new User($res);
							
							$_SESSION['logged'] = true;
						//	header('Location: index.php');
						
						}else{
							echo 'mauvais user ou mdp';
						}
					}elseif (isLogged() && isset($_POST["logout"])){
						unset($_SESSION['logged']);
					//	header('Location: index.php');
					}
					
				}elseif($this->page == "creation_theme"){
					if(isset($_POST['name'])){
						$dao->createTheme($_POST['name']);
					}	
				}elseif($this->page == "admin"){
					$this->config['subMenuItems'] = array(
							array("ref" => "/admin/images", "name" => "images"),
							array("ref" => "/admin/upload", "name" => "upload"),
							array("ref" => "/admin/creation_theme", "name" => "création de theme"),
							array("ref" => "/admin/administration_theme", "name" => "Administration des de thèmes")
							
					);
				}elseif($this->page == "createArtFromPath"){

					$thechniques =$dao->getAll('technique');
					$themes = $dao->getAllThemes();
					$this->config['themes'] = $themes;
					$this->config['techniques'] = $thechniques;
				}else{
                    $this->config['data'] = $this->getData($this->page);
				}
				if($this->page == "images"){
					$all_media = $dao->getAllOeuvres();	
					
					
					$this->config['all_media_map'] = array();
					foreach ($all_media as $m){
						
						$this->config['all_media_map'][$m->get_path()] = true;
					}
				}
				if($this->config['haveGallery']){
					if(isLogged() ){
						
					
						$themes = $dao->getAllThemes();
						$this->config['themes'] = $themes;
					}
					$this->config['gallery'] = $dao->getOeuvres($this->page);
				
				}
				if($this->config['haveMenu']){
					$this->config['menuItems']  = array(array("ref" => "/accueil", "name" => "Acceuil","img_path" => "/rsc/img/fleur_1.jpg" ));
					$result2 = $db->query("select theme.* from theme join page_config on theme.name = page_config.name where parent_theme_key is null");
					while($res = $result2->fetchArray(SQLITE3_ASSOC)){
						$this->config['menuItems'][] = array("ref" => "/".str_replace(" ","-",$res['name'])."", "name" => ucfirst($res['name']) );
					}
					$result2->finalize();
					$this->config['menuItems'][] = array("ref" => "/apropos", "name" => "A propos" );
						
					$this->config['menuItems'][] =array("ref" => "/contact", "name" => "Contact" );
					if(isLogged()){
						$this->config['menuItems'][] =array("ref" => "/admin", "name" => "admin" );

					}
				}
				
				if($this->config['haveSubMenu'] && $this->page != "admin"){
                    $themesChilds = $dao->getThemesByThemeParent($this->page);
                    
					$this->config['subMenuItems']  = Config::makeItems($themesChilds);

					
				}
				
		
				$result2 = $db->query("select * from page_css where page = '". $this->page."' or page = 'DEFAULT'");
				while($res2 = $result2->fetchArray(SQLITE3_ASSOC)){
					$this->config['cssItems'][] = array("ref" => "/rsc/css/".$res2['css']);
		
				}
				$result2->finalize();
				$result2 = $db->query("select * from page_js where page = '". $this->page."'or page = 'DEFAULT' order by order_js");
				while($res2 = $result2->fetchArray(SQLITE3_ASSOC)){
					$this->config['javascript'][] = array("ref" => "/rsc/js/".$res2['js']);
		
				}
				$result2->finalize();
		
			}

			return $this->config != false;
			 
			//print_r($this->config);
		} catch (Exception $e) {
			echo 'Exception reçue : ',  $e->getMessage(), "\n";
		} finally {
			if(isset($db)){
				$db->close();
			}
			 
		}
	}
	
	public static function makeItems($nomables){
        $ret = array();
        foreach($nomables as $nomable){
            $ret[] = array("ref" => "/".str_replace(" ","-",$nomable->get_name())."", "name" => ucfirst($nomable->get_name()), "img" => $nomable->get_image_path());
        }
        return $ret;
        
	}
}
?>
