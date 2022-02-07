project 'Bill of Materials',{
	application "Sample App",{
		property "releaseBoard",{
			property "1.0",{
				_status = "production"
				compA = "1.0"
				compB = "1.2"
			}
			property "2.0",{
				_status = "candidate"
				compA = "2.0"
				compB = "2.2"
			}
			property "3.0",{
				_status = "archived"
				compA = "3.0"
				compB = "3.2"
			}
		}
	}
}