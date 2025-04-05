import { TooltipProvider } from "@sonarsource/echoes-react";
import { createRoot } from "react-dom/client";
import { IntlProvider } from "react-intl";
import Configuration from "./configuration/configuration";

window.registerExtension("licensecheck/configuration", function (options) {
  const root = createRoot(options.el);
  root.render(
    <IntlProvider locale="en">
      <TooltipProvider>
        <Configuration options={options} />
      </TooltipProvider>
    </IntlProvider>,
  );

  return function () {
    root.unmount();
  };
});
