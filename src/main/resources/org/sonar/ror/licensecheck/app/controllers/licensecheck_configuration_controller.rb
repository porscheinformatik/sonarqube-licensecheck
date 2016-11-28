class LicensecheckConfigurationController < ApplicationController
  SECTION = Navigation::SECTION_CONFIGURATION
  before_filter :login_required

  def index
    licenses
  end

  def mavenDependencies
    render :template => 'licensecheck_configuration/mavenDependencies'
  end

  def licenses
    render :template => 'licensecheck_configuration/licenses'
  end

  def mavenLicenses
    render :template => 'licensecheck_configuration/mavenLicenses'
  end

  def projects
    render :template => 'licensecheck_configuration/projectLicenses'
  end

end
