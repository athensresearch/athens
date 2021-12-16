import React from "react";
import styled from "styled-components";

import { Button } from "@/Button";
import { Heading, Actions, PageWrapper, TextField } from "../Welcome";

import { Plus, Wifi } from "iconoir-react";

const Preview = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  gap: 0.5rem;
  flex: 0 0 auto;
  margin: auto;
`;

const IconWrap = styled.div`
  width: 5em;
  height: 5em;
  border-radius: 18%;
  margin: 0 auto 1rem;
  border: 2px solid var(--link-color);
  display: flex;
  place-content: center;
  place-items: center;
  color: var(--link-color);
  position: relative;

  svg {
    position: absolute;
    width: 4rem;
    height: 4rem;

    + svg {
      bottom: -10%;
      right: -10%;
      width: 1.75rem;
      height: 1.75rem;
      background: var(--link-color);
      color: var(--link-color---contrast);
      border-radius: 1000em;
    }
  }
`;

const EmptyLogin = { address: "", password: "" };

export interface JoinDatabaseProps {
  onLogin: (login) => void;
  onGoBack: () => void;
}

export const JoinDatabase = React.forwardRef(
  (props: JoinDatabaseProps, ref): JSX.Element => {
    const { onLogin: handleLogin, onGoBack: handleGoBack } = props;
    const [login, setLogin] = React.useState(EmptyLogin);

    return (
      <PageWrapper ref={ref}>
        <Preview>
          <Heading>Join a Shared Database</Heading>
          <IconWrap>
            <Wifi strokeWidth={1} />
            <Plus strokeWidth={2} />
          </IconWrap>
          <TextField
            placeholder={"Address"}
            onChange={(e) =>
              setLogin({ ...login, address: e.target.value.trim() })
            }
          />
          <TextField
            placeholder={"Password"}
            type="password"
            onChange={(e) => setLogin({ ...login, password: e.target.value })}
          />
        </Preview>
        <Actions>
          <Button shape="round" variant="gray" onClick={() => handleGoBack()}>
            Cancel
          </Button>
          <Button
            disabled={login.address == ""}
            shape="round"
            variant="filled"
            onClick={() => handleLogin(login)}
          >
            Join
          </Button>
        </Actions>
      </PageWrapper>
    );
  }
);
