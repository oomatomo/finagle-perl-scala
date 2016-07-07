use utf8;
use strict;
use warnings;
use Amon2::Lite;
use JSON::XS;

any '/account/json' => sub {
    my $c   = shift;
    if ($c->session->get("hoge_session_id") eq "1") {
        return $c->create_response(
            200, [],
            [JSON::XS->new->utf8(0)->encode({ id => 100, name => "hoge" })]
        );
    } else {
        return $c->create_response(
            401, [],
            [JSON::XS->new->utf8(0)->encode({ error => "auth error" })]
        );
    }
};

get '/account/login' => sub {
    my $c = shift;
    $c->session->set('hoge_session_id' => 1);
    return $c->create_response(200, [], ["login ok"]);
};
__PACKAGE__->enable_session();
__PACKAGE__->to_app();
