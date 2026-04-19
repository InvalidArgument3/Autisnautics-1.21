PlayerEvents.loggedIn(event => {
    event.player.tell(["Welcome to ", Text.gold("Autisnautics II"), " on 1.20"]);
    event.player.tell(["Report pack issues to ", Text.gold("the Github").underlined().clickOpenUrl("https://github.com/InvalidArgument3/Autisnautics2").hover("Click to open"), "."]);
})
