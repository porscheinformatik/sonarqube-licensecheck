require 'csv'

class LicensecheckExportController < ApplicationController

  def index
    init_resource_for_user_role

    helper = Api::Utils.java_facade.getComponentByClassname('licensecheck', 'at.porscheinformatik.sonarqube.licensecheck.widget.WidgetHelper')
    depMeasure = @snapshot.measure('licensecheck.dependency').data
    @dependencies = helper.getDependencies(depMeasure)

    licMeasure = @snapshot.measure('licensecheck.license').data
    @licenses = helper.getLicenses(licMeasure)

    respond_to do |format|
      format.csv { send_data csv_string(), :filename => "#{@resource.key}.csv", :type => 'text/csv' }
      format.xls { response.headers['Content-Disposition'] = "attachment; filename=\"#{@resource.key}.xml\"" }
    end
  end

  def csv_string

    csv = "\"Name\";\"Version\";\"License\";\"Status\"\n"
    @dependencies.each do |dep|
      csv += "\"" + dep.name + "\";\"" + dep.version + "\";\"" + dep.license + "\";\"" + dep.status + "\"\n"
    end

    return csv
  end

end
