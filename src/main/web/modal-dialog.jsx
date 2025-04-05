import { useEffect } from "react";

const ModalDialog = ({ header, show, onClose, children, footer }) => {
  useEffect(() => {
    const handleEscape = (e) => {
      if (show && e.keyCode === 27) {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [show, onClose]);

  if (!show) return null;

  return (
    <>
      <div className="modal in">
        <div className="modal-head">
          <h2>{header}</h2>
        </div>
        <div className="modal-body">{children}</div>
        <div className="modal-foot">
          {footer}
          <a href="#" className="button button-link" onClick={onClose}>
            Cancel
          </a>
        </div>
      </div>
      <div className="modal-overlay in"></div>
    </>
  );
};

export default ModalDialog;
