class LicensecheckConfigurationController < ApplicationController
  SECTION = Navigation::SECTION_CONFIGURATION
  before_filter :admin_required
  
  def index
    licenses
  end

  def mavendependency
    @allowedDependencies = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.dependency.DependencySettingsService').getAllowedDependenciesForTable()
    @licensesIdentifier = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').getLicensesID()

    render :template => 'licensecheck_configuration/mavendependency'
  end

  def licenses
    @licenses = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').getLicensesForTable()

    render :template => 'licensecheck_configuration/licenses'
  end

  def export_csv
    @entries = load_dependencies
    @licenseForExport = load_licenses
    @filename = 'filename.csv'

    render :template => 'licensecheck_configuration/export_csv', :layout => false
  end

  def mavenlicenses
    @licensesIdentifier = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').getLicensesID()
    @licensesregex = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').getLicensesRegexForTable()

    render :template => 'licensecheck_configuration/mavenlicenses'
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

  def add_dependencies
    key = params['key']
    license = params['license']

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.dependency.DependencySettingsService').addAllowedDependency(key, license)

    redirect_to :action => 'mavendependency'
  end

  def delete_dependencies
    key = params[:alloweddependency_tag]

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.dependency.DependencySettingsService').deleteAllowedDependency(key)

    redirect_to :action => 'mavendependency'
  end

  def add_licenses
    identifier = params['identifier']
    name = params['name']
    status = params['status']

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').addLicense(identifier, name, status)

    redirect_to :action => 'licenses'
  end

  def delete_licenses
    identifier = params[:license_tag]

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').deleteLicense(identifier)

    redirect_to :action => 'licenses'
  end

  def update_maven_licenses
    id = params[:id]
    name = params[:name]
    status = params[:newidentifier]

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').updateMavenLicense(id, name, status)

    render :text => "OK"
  end

  def update_licenses
    id = params[:id]
    name = params[:name]
    status = params[:status]

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').updateLicense(id, name, status)

    render :text => "OK"
  end

  def add_license_regex
    license_name = params['license_name']
    license_key = params['license']

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').addLicenseRegex(license_name, license_key)

    redirect_to :action => 'mavenlicenses'
  end

  def delete_license_regex
    key = params[:licensesregex_tag]

    Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.license.LicenseSettingsService').deleteLicenseRegex(key)

    redirect_to :action => 'mavenlicenses'
  end
end