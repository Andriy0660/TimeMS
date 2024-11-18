import {useState} from 'react';
import Button from '@mui/material/Button';
import {Link} from "react-router-dom";
import TextField from '@mui/material/TextField';
import {useMutation} from "@tanstack/react-query";
import authApi from "../api/authApi.js";
import useAppContext from "../context/useAppContext.js";
import {LinearProgress} from "@mui/material";
import useAsyncCall from "../hooks/useAsyncCall.js";
import Card from "../components/auth/Card.jsx";
import GoogleIcon from '@mui/icons-material/Google';
import Divider from '@mui/material/Divider';

export default function LogInPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [emailError, setEmailError] = useState(false);
  const [emailErrorMessage, setEmailErrorMessage] = useState("");
  const [passwordError, setPasswordError] = useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = useState("");
  const {addAlert} = useAppContext();

  const {mutateAsync: onLogIn} = useMutation({
    mutationFn: (body) => authApi.logIn(body),
    onSuccess: async ({accessToken}) => {
      localStorage.setItem("token", accessToken);
      addAlert({
        text: "Logged in successfully",
        type: "success"
      });
      window.location.href = "/app/timelog";
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Logging in failed:", error);
    }
  });

  const {execute: handleLogIn, isExecuting: isLoggingIn} = useAsyncCall({
    fn: async () => {
      if (validateInputs()) {
        await onLogIn({email, password});
      }
    },
    onFinally: () => {
      setEmail("");
      setPassword("");
    }
  });

  const validateInputs = () => {
    let isValid = true;

    if (!email || !/\S+@\S+\.\S+/.test(email)) {
      setEmailError(true);
      setEmailErrorMessage("Please enter a valid email address.");
      isValid = false;
    } else {
      setEmailError(false);
      setEmailErrorMessage("");
    }

    if (!password || password.length < 6) {
      setPasswordError(true);
      setPasswordErrorMessage("Password must be at least 6 characters long.");
      isValid = false;
    } else {
      setPasswordError(false);
      setPasswordErrorMessage("");
    }

    return isValid;
  };

  return (
    <div className="min-h-screen flex">
      <Card>
        <div className="text-center text-4xl">Log in</div>
        <div className="flex flex-col">
          <TextField
            className="my-4"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            required
            fullWidth
            placeholder="your@email.com"
            error={emailError}
            helperText={emailErrorMessage}
            color={emailError ? "error" : "primary"}
          />
          <TextField
            className="mb-4"
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
            fullWidth
            placeholder="••••••"
            type="password"
            error={passwordError}
            helperText={passwordErrorMessage}
            color={passwordError ? "error" : "primary"}
          />
          <Button
            className="my-2"
            type="submit"
            fullWidth
            variant="contained"
            onClick={handleLogIn}
          >
            Log in
          </Button>
          <div className="text-center">
            Don&apos;t have an account?{" "}
            <span>
              <Link to="/app/signup/">
                Sign up
              </Link>
            </span>
          </div>
          {isLoggingIn && <LinearProgress className="mt-2" />}
        </div>
        <Divider>or</Divider>
        <Button
          fullWidth
          variant="outlined"
          startIcon={<GoogleIcon />}
        >
          Log in with Google
        </Button>
      </Card>
    </div>
  );
}
