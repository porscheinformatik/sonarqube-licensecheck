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


  def export_used_dependencies_csv
    @dependenciesForExport = load_dependencies
    @filename = 'used_dependencies.csv'

    render :template => 'licensecheck_configuration/export_used_dependencies_csv', :layout => false
  end

  def export_used_licenses_csv
    @licenseForExport = load_licenses
    @filename = 'used_licenses.csv'

    render :template => 'licensecheck_configuration/export_used_licenses_csv', :layout => false
  end

  def load_dependencies
    dependencyMeasure = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.widget.DependencyCheckWidgetValidator').getDependencyString()

    entries = []
    checkedDependencies = dependencyMeasure.split(';')
    checkedDependencies.each do |dependency|
      d = dependency.split('~')
      name = d[0]
      version = d[1]
      license = d[2]
      entries.push([name, version, license])
    end
    entries
  end

  def load_licenses
    licenseMeasure =  Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.widget.DependencyCheckWidgetValidator').getLicenseString()

    entries = []
    checkedLicenses = licenseMeasure.split(';')
    checkedLicenses.each do |license|
      l = license.split('~')
      identifier = l[0]
      name = l[1]
      status = l[2]
      entries.push([identifier, name, status])
    end
    entries.uniq
  end

end
