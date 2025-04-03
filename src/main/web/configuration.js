import { createRoot } from "react-dom/client";
import Configuration from "./configuration/configuration";

window.registerExtension("licensecheck/configuration", function (options) {
  const root = createRoot(options.el);
  root.render(<Configuration options={options} />);

  return function () {
    root.unmount();
  };
});
