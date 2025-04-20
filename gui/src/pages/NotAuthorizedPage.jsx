import { Typography, Paper, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LockIcon from '@mui/icons-material/Lock';

export default function NotAuthorizedPage() {
  const navigate = useNavigate();

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="70vh"
    >
      <Paper elevation={3} className="p-10 max-w-lg text-center">
        <LockIcon sx={{ fontSize: 60, color: 'error.main', mb: 2 }} />

        <Typography variant="h4" gutterBottom className="text-red-500 font-bold">
          Access Denied
        </Typography>

        <Typography variant="body1" paragraph>
          You don't have permission to access this page. This area requires elevated privileges.
        </Typography>

        <Typography variant="body2" paragraph className="text-gray-600">
          If you believe you should have access to this page, please contact your administrator.
        </Typography>

        <Box mt={4} display="flex" justifyContent="center" gap={2}>

          <Button
            variant="outlined"
            onClick={() => navigate("/")}
          >
            Go Back
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}