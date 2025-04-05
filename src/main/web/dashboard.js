import { TooltipProvider } from "@sonarsource/echoes-react";
import { createRoot } from "react-dom/client";
import { IntlProvider } from "react-intl";
import Dashboard from "./dashboard/dashboard";

window.registerExtension("licensecheck/dashboard", function (options) {
  const root = createRoot(options.el);
  root.render(
    <IntlProvider locale="en">
      <TooltipProvider>
        <Dashboard options={options} />
      </TooltipProvider>
    </IntlProvider>,
  );

  return function () {
    root.unmount();
  };
});
