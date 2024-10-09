import {CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import Button from "@mui/material/Button";
import {useState} from "react";

export default function ConfirmationModal({
  open,
  type,
  children,
  actionText,
  onConfirm,
  onCancel,
  onClose,
}) {

  const [isLoading, setIsLoading] = useState(false);

  if (!open) {
    return null;
  }

  async function handleConfirm() {
    setIsLoading(true);
    try {
      await onConfirm();
    } catch (error) {
      console.error("Error:", error);
    } finally {
      setIsLoading(false);
      onClose()
    }
  }

  const typeColorConfig = {
    error: "error",
    info: "primary",
  };
  const color = typeColorConfig[type] || "primary";

  return (
    <Dialog open={true} onClose={onClose} onClick={e => e.stopPropagation()}>
      <DialogTitle className="pt-4 flex justify-center">
        <ExclamationMark />
      </DialogTitle>

      <DialogContent>
        <DialogContentText>
          {children}
        </DialogContentText>
      </DialogContent>

      <DialogActions className="mb-2 mr-2">
        <Button
          variant="outlined"
          color="info"
          size="large"
          onClick={() => {
            if (onCancel) {
              onCancel();
            }
            onClose();
          }}
        >
          Cancel
        </Button>
        {isLoading ? <CircularProgress size={25} /> : <Button
          color={color}
          variant="contained"
          size="large"
          onClick={handleConfirm}
        >
          {actionText}
        </Button>
        }
      </DialogActions>
    </Dialog>
  );
}

function ExclamationMark() {
  return <svg
    className="mx-auto mb-4 text-gray-400 w-12 h-12 dark:text-gray-200"
    aria-hidden="true"
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 20 20"
  >
    <path
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M10 11V6m0 8h.01M19 10a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
    />
  </svg>;
}
