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

export default function SignUpPage() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [emailError, setEmailError] = useState(false);
  const [emailErrorMessage, setEmailErrorMessage] = useState("");
  const [passwordError, setPasswordError] = useState(false);
  const [passwordErrorMessage, setPasswordErrorMessage] = useState("");
  const [firstNameError, setFirstNameError] = useState(false);
  const [firstNameErrorMessage, setFirstNameErrorMessage] = useState("");
  const {addAlert} = useAppContext();

  const {mutateAsync: onSignUp} = useMutation({
    mutationFn: (body) => authApi.signUp(body),
    onSuccess: async () => {
      addAlert({
        text: "Account was successfully created",
        type: "success"
      });
    },
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating account failed:", error);
    }
  });

  const {execute: handleSignUp, isExecuting: isCreating} = useAsyncCall({
    fn: async () => {
      if (validateInputs()) {
        await onSignUp({firstName, lastName, email, password});
      }
    },
    onFinally: () => {
      setFirstName("");
      setLastName("");
      setEmail("");
      setPassword("");
    },
  })

  const validateInputs = () => {
    let isValid = true;

    if (!firstName || firstName.length < 1) {
      setFirstNameError(true);
      setFirstNameErrorMessage("First name is required.");
      isValid = false;
    } else {
      setFirstNameError(false);
      setFirstNameErrorMessage("");
    }

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
        <div className="text-center text-4xl">Sign Up</div>
        <div className="flex flex-col">
          <TextField
            className="my-4"
            label="First Name"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            autoComplete="name"
            required
            fullWidth
            placeholder="John"
            error={firstNameError}
            helperText={firstNameErrorMessage}
            color={firstNameError ? "error" : "primary"}
          />
          <TextField
            className="mb-4"
            label="Last Name"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            autoComplete="name"
            fullWidth
            placeholder="Snow"
          />
          <TextField
            className="mb-4"
            label="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            fullWidth
            placeholder="your@email.com"
            autoComplete="email"
            variant="outlined"
            error={emailError}
            helperText={emailErrorMessage}
            color={passwordError ? "error" : "primary"}
          />
          <TextField
            className="mb-4"
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            fullWidth
            placeholder="••••••"
            type="password"
            variant="outlined"
            error={passwordError}
            helperText={passwordErrorMessage}
            color={passwordError ? "error" : "primary"}
          />

          <Button
            className="my-2"
            type="submit"
            fullWidth
            variant="contained"
            onClick={handleSignUp}
          >
            Sign up
          </Button>
          <div className="text-center">
            Already have an account?{" "}
            <span>
                <Link to="/app/login/">
                  Log in
                </Link>
              </span>
          </div>
          {isCreating && <LinearProgress className="mt-2"/>}
        </div>

      </Card>
    </div>
  );
}