import React from "react";
import styled, { keyframes } from "styled-components";

import { AddBox, Wifi } from "@material-ui/icons";
import { Slide, Fade } from "@material-ui/core";

import { Button, ButtonWrap } from "@/Button";
import { Overlay } from "@/Overlay";
import { Input } from "@/Input";
import { DatabasesList } from "./components/DatabasesList";
import { AddDatabase } from "./components/AddDatabase";
import { JoinDatabase } from "./components/JoinDatabase";

const WelcomeWrap = styled(Overlay)`
  height: min(90vh, 32rem);
  width: min(90vw, 40rem);
  display: flex;
  padding: 0;
  overflow: hidden;
  flex-direction: row;
  user-select: none;
  margin: auto;
  background: transparent;
  border-radius: 1rem;
`;

const Sidebar = styled.aside`
  display: flex;
  flex-direction: column;
  flex: 0 0 12em;
  background: var(--background-plus-1);
  border-top-left-radius: inherit;
  border-bottom-left-radius: inherit;
  --webkit-overflow-scrolling: touch;

  @supports (backdrop-filter: blur(20px)) {
    background: var(--background-color---opacity-high);
    backdrop-filter: blur(20px);
  }

  header {
    padding: 2rem 1rem;
    display: flex;
    flex-direction: column;
  }
`;

export const Main = styled.main`
  --main-padding-sides: 1rem;
  flex: 1 1 100%;
  background: var(--background-color);
  display: grid;
  grid-template-areas: "main";
  overflow-y: auto;
`;

const Logo = styled.span`
  padding: 0 1rem;
  font-family: var(--font-family-serif);
  color: var(--body-text-color---opacity-med);
  font-weight: bold;
  font-size: 2rem;
`;

const Version = styled.span`
  color: var(--body-text-color---opacity-med);
  font-size: var(--font-size--text-sm);
  padding: 0 1rem;
`;

export const PageWrapper = styled(Overlay).attrs({
  hasOutline: false,
})`
  grid-area: main;
  display: flex;
  margin: 1rem;
  align-self: stretch;
  justify-self: stretch;
  flex-direction: column;
  align-items: stretch;
  min-height: calc(100% - 2rem);
  height: max-content;
`;

const WelcomeActions = styled.div`
  display: grid;
  grid-auto-flow: row;
  margin-top: auto;

  ${ButtonWrap} {
    justify-content: flex-start;
    padding: 1rem 2rem;
    border-radius: 0;
    color: var(--body-text-color);
    gap: 1.5ch;

    svg {
      position: relative;
      margin-left: -0.75rem;
      padding: 0.35rem;
      display: inline-block;
      width: 2rem;
      height: 2rem;
      background: var(--link-color);
      color: var(--link-color---contrast);
      border-radius: 10em;
    }
  }
`;

export const Actions = styled.div`
  display: grid;
  grid-auto-columns: 1fr;
  grid-auto-flow: column;
  gap: 1rem;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  padding: 1rem 0;
  margin: auto auto 0;
`;

export const Header = styled.div`
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  margin-bottom: auto;
  padding: 0.5rem;
  justify-content: flex-end;

  ${ButtonWrap} {
    color: var(--body-text-color---opacity-high);
    font-size: var(--font-size--text-sm);
  }
`;

export const pulseInputOutline = keyframes`
  from {
    box-shadow: 0 0 0 2px var(--link-color---opacity-med);
  } to {
    box-shadow: 0 0 0 2px var(--link-color);
  }
`;

export const TextField = styled(Input)`
  text-align: center;
  border-radius: 100em;
  transition: all 0.2s ease-in-out;

  &:focus {
    outline: none;
    animation: ${pulseInputOutline} 1s ease-in-out alternate infinite;
  }
`;

export const Heading = styled.h2`
  font-size: 1.5rem;
  color: var(--body-text-color---opacity-high);
`;

type WelcomeView = "databases-list" | "join-database" | "add-database";

interface WelcomeProps {
  defaultView: WelcomeView;
  databases: Database[];
  currentDatabaseId: string;
  onLogin: (login) => void;
  onCreateDatabase: (database: Database) => void;
  onChooseDatabase: (database: Database) => void;
  onDeleteDatabase: (database: Database) => void;
  onRenameDatabase: (database: Database) => void;
  onRemoveDatabase: (database: Database) => void;
}

export const Welcome = (props: WelcomeProps) => {
  const {
    defaultView = "databases-list",
    currentDatabaseId,
    databases,
    onLogin: handleLogin,
    onCreateDatabase: handleCreateDatabase,
    onChooseDatabase: handleChooseDatabase,
    onDeleteDatabase: handleDeleteDatabase,
    onRemoveDatabase: handleRemoveDatabase,
    onRenameDatabase: handleRenameDatabase,
  } = props;

  const [view, setView] = React.useState(defaultView);
  const currentDatabase = databases?.find((db) => db.id === currentDatabaseId);
  const recentDatabases = databases?.filter(
    (db) => db.id !== currentDatabaseId
  );

  // Switch to Add Database if no databases exist and view hasn't been manually set
  React.useEffect(() => {
    if (!recentDatabases && !currentDatabaseId) {
      setView("add-database");
    }
  }, [currentDatabaseId, recentDatabases]);

  return (
    <WelcomeWrap>
      <Sidebar>
        <header>
          <Logo>Athens</Logo>
          <Version>1.0.0</Version>
        </header>

        <WelcomeActions>
          <Button
            isPressed={view === "add-database"}
            onClick={() => setView("add-database")}
          >
            <AddBox />
            Add
          </Button>
          <Button
            isPressed={view === "join-database"}
            onClick={() => setView("join-database")}
          >
            <Wifi />
            Join
          </Button>
        </WelcomeActions>
      </Sidebar>
      <Main>
        <Fade
          in={view === "databases-list"}
          mountOnEnter
          unmountOnExit
          appear={false}
        >
          <DatabasesList
            currentDatabase={currentDatabase}
            recentDatabases={recentDatabases}
            onChooseDatabase={handleChooseDatabase}
            onDeleteDatabase={handleDeleteDatabase}
            onRenameDatabase={handleRenameDatabase}
            onRemoveDatabase={handleRemoveDatabase}
            onNavToAdd={() => setView("add-database")}
            onNavToJoin={() => setView("join-database")}
          />
        </Fade>
        <Slide
          direction="up"
          in={view === "add-database"}
          mountOnEnter
          unmountOnExit
        >
          <AddDatabase
            onGoBack={() => setView("databases-list")}
            onAddFromFile={(database) => console.log("add from file", database)}
            onCreateDatabase={handleCreateDatabase}
          />
        </Slide>
        <Slide
          direction="up"
          in={view === "join-database"}
          mountOnEnter
          unmountOnExit
        >
          <JoinDatabase
            onGoBack={() => setView("databases-list")}
            onLogin={handleLogin}
          />
        </Slide>
      </Main>
    </WelcomeWrap>
  );
};
