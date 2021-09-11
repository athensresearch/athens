import React from 'react';
import styled from 'styled-components'

import { Overlay } from '../../Overlay';
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
    justify-content: space-between;
    padding: 2rem;
    flex: 0 0 12em;
    background: var(--background-plus-1);
    border-top-left-radius: inherit;
    border-bottom-left-radius: inherit;
    --webkit-overflow-scrolling: touch;

    @supports (backdrop-filter: blur(20px)) {
        background: var(--background-color---opacity-med);
        backdrop-filter: blur(20px);
    }
`;

const Logo = styled.span`
    margin-bottom: auto;
    font-family: var(--font-family-serif);
    font-weight: bold;
`;

const Version = styled.span`
    color: var(--body-text-color---opacity-med);
    font-size: var(--font-size--text-sm);
`;

const Main = styled.main`
    padding: 2rem;
    overflow-y: auto;
    flex: 1 1 100%;
    border-top-right-radius: inherit;
    border-bottom-right-radius: inherit;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    background: var(--background-color);
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
`;

export const Welcome = ({ databases }) => {
    return <WelcomeWrap>
        <Sidebar>
            <Logo>Athens</Logo>
            <Version>1.0.0</Version>
        </Sidebar>
        <Main>
            <Section>
                <List>
                    {databases.map(db => (
                        <Item {...db} />
                    ))}
                </List>
            </Section>
        </Main>
    </WelcomeWrap>
    
}