import styled from 'styled-components'

import { AddBox, Wifi } from '@material-ui/icons'

import { Button } from '@/Button';
import { Overlay } from '@/Overlay';
import { Item } from './components/Item';

const WelcomeWrap = styled(Overlay)`
    height: min(90vh, 30em);
    width: min(90vw, 40em);
    display: flex;
    padding: 0;
    overflow: hidden;
    flex-direction: row;
    user-select: none;
    margin: auto;
    background: transparent;
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
        background: var(--background-color---opacity-med);
        backdrop-filter: blur(20px);
    }

    header {
        padding: 2rem 1rem;
        display: flex;
        flex-direction: column;
    }
`;

const Logo = styled.span`
    padding: 0 1rem;
    font-family: var(--font-family-serif);
    font-weight: bold;
`;

const Version = styled.span`
    color: var(--body-text-color---opacity-med);
    font-size: var(--font-size--text-sm);
    padding: 0 1rem;
`;

const Main = styled.main`
    padding: 1.5rem 2rem 2rem;
    overflow-y: auto;
    flex: 1 1 100%;
    border-top-right-radius: inherit;
    border-bottom-right-radius: inherit;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    background: var(--background-color);
`;

const Heading = styled.h2`
    font-size: var(--font-size--text-sm);
    letter-spacing: 0.03em;
    padding-left: 0.5rem;
    color: var(--body-text-color---opacity-med);
`;

const Section = styled.section`
    display: flex;
    flex-direction: column;
    align-items: stretch;
`;

const List = styled.ol`
    padding: 0;
    margin: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
`;

const Actions = styled.div`
    display: grid;
    grid-auto-flow: row;
    margin-top: auto;

    ${Button} {
        justify-content: flex-start;
        padding: 1rem 2rem;
        border-radius: 0;
        color: var(--body-text-color);

        svg {
            position: relative;
            margin-right: 0.125rem;
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

export const Welcome = ({
    databases,
    handlePressCreateDatabase,
    handlePressJoinDatabase,
}) => {

    return <WelcomeWrap>
        <Sidebar>
            <header>
                <Logo>Athens</Logo>
                <Version>1.0.0</Version>
            </header>

            {databases && (
                <Actions>
                    <Button
                        onClick={handlePressCreateDatabase}>
                        <AddBox />
                        Create New
                    </Button>
                    <Button
                        onClick={handlePressJoinDatabase}>
                        <Wifi />
                        Join
                    </Button>
                </Actions>
            )}
        </Sidebar>
        <Main>
            {databases ? (
                <Section>
                    <Heading>Recent</Heading>
                    <List>
                        {databases.map(db => (
                            <Item db={db} onChooseDatabase={() => console.log('test')} />
                        ))}
                    </List>
                </Section>
            ) : (
                <Section>
                    <Button
                        onClick={handlePressCreateDatabase}
                        variant="filled"
                    >
                        <AddBox />
                        <span>Create a Database</span>
                    </Button>
                    <Button
                        onClick={handlePressJoinDatabase}
                        variant="filled"
                    >
                        <Wifi />
                        <span>Join</span>
                    </Button>
                </Section>
            )}
        </Main>
    </WelcomeWrap>

}