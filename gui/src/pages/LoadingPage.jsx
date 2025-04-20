// gui/src/components/general/LoadingPage.jsx
import { Box, CircularProgress, Typography } from '@mui/material';

export default function LoadingPage() {
  return (
    <Box
      display="flex"
      flexDirection="column"
      justifyContent="center"
      alignItems="center"
      minHeight="40vh"
    >
      <CircularProgress size={60} thickness={4} />
      <Typography variant="h6" sx={{ mt: 2 }}>
        Loading...
      </Typography>
    </Box>
  );
}