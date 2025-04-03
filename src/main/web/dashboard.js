import { createRoot } from "react-dom/client";
import Dashboard from "./dashboard/dashboard";

window.registerExtension("licensecheck/dashboard", function (options) {
  const root = createRoot(options.el);
  root.render(<Dashboard options={options} />);

  return function () {
    root.unmount();
  };
});
