project 'Default'
procedure 'Application Decision Gate', {
  description = '### Compliance gate procedure - this procedure uses a hidden API not intended for production use ###'
  projectName = 'Default'
  timeLimit = '0'

  formalParameter 'externalApplicationId', defaultValue: '', {
    orderIndex = '1'
    required = '1'
    type = 'entry'
  }

  formalParameter 'releaseManifest', defaultValue: '', {
    orderIndex = '2'
    required = '1'
    type = 'textarea'
  }

  step 'Application Specific Compliance Check ', {
    command = '''use strict;
use ElectricCommander;
use Switch;
use XML::XPath;

# Turn off output buffering
$| = 1;

my $ec = new ElectricCommander({abortOnError => 0});
my $xpath = $ec->getActualParameter("releaseManifest",
        {"jobId" => "$[/myJob/jobId]"});
my $releaseManifest = $xpath->findvalue("//value")->value;

while (1) {

    print "Checking Compliance gate decision...\\n";
    #<externalApplicationId> <flowRuntimeStateId> <releaseManifest>
    $xpath = $ec->getComplianceGateDecision(
            "$[externalApplicationId]",
            "$[/myJob/flowRuntimeStateId]",
            $releaseManifest
            );
    my $errors = $ec->checkAllErrors($xpath);

    if ($errors) {
      $ec->setProperty("summary", $errors);
      print "$errors\\n";
      exit 1;
    }

    my $result = $xpath->findvalue("//result")->value;
    my $summary = $xpath->findvalue("//summary")->value;
    my $linkBackUrl = $xpath->findvalue("//linkBackUrl")->value;
    print "Result: $result\\n";
    print "Summary: $summary\\n";
    if ($linkBackUrl) {
      print "Compliance URL: $linkBackUrl\\n";
    }


    if ($summary) {
     $ec->setProperty("summary", "Gate decision: $summary");
     $ec->setProperty("/myPipelineStageRuntime/ec_summary/Exit gate decision", $summary);

     if ($linkBackUrl) {
       $ec->setProperty("/myJob/report-urls/Compliance gate report",  $linkBackUrl);
       eval {
          my $link = qq|<html><span class=\\"jobStep_statusText\\"><a target=\\"_blank\\" href=\\"$linkBackUrl\\">$summary</a></span><br /></html>|;

         $ec->setProperty("/myPipelineStageRuntime/ec_summary/Exit gate decision", $link);

       };
      }
    }




    switch ($result) {
       case("success") {
        exit 0;
       }
       case("failed") {
        exit 1;
       }
       case("error") {
        exit 1;
       }
       case("waiting") {
        print "Waiting for result...\\n";
        sleep(10);
       }
       default {
        print "Unexpected result: $result\\n";
        exit 1;
       }
    }
}'''
    shell = 'ec-perl'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}
